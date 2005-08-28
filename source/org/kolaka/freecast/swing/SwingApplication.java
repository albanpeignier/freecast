/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2005 Alban Peignier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.kolaka.freecast.swing;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.Application;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Timer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class SwingApplication extends Application {

	public SwingApplication(String name) {
		super(name);
	}

	protected void displayFatalError(Throwable cause) {
	    super.displayFatalError(cause);

		String stackTrace = ExceptionUtils.getFullStackTrace(cause).replaceAll("\n", "<br>");

	    String message =
	            "<html><p>FreeCast can't start because of the following error:</p>&nbsp;"
	            + "<p><b>" + cause.getMessage() + "</b><br>" + stackTrace + "</p>&nbsp;"
	            + "<p>If needed, visit http://www.freecast.org/support</p>&nbsp;";
	    JOptionPane.showMessageDialog(null, message, "FreeCast Node can't start", JOptionPane.ERROR_MESSAGE);
	}

	protected void displayHelper(String message, String usage) {
	    boolean error = false;

	    StringBuffer sb = new StringBuffer("<html>");
	    if (!StringUtils.isEmpty(message)) {
	        error = true;
	        sb.append("<p>").append(message).append("</p>&nbsp;");
	    }
	    sb.append("<p>").append(usage);

	    JOptionPane.showMessageDialog(null, sb.toString(), "FreeCast Help Usage",
	            error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
	}

	protected void postInit(Configuration configuration) throws Exception {
		initDefaultFont();
	}

	protected abstract void exitImpl() throws Exception;

	private void initDefaultFont() throws IOException,
	        FontFormatException {
	    InputStream resource = SwingApplication.class
	            .getResourceAsStream("resources/ttf-bitstream-vera.ttf");
	    Font defaultFont = Font.createFont(Font.TRUETYPE_FONT, resource);
	    FontUIResource fontUIResource = new FontUIResource(defaultFont
	            .deriveFont(12.0f));

	    Enumeration keys = UIManager.getDefaults().keys();
	    while (keys.hasMoreElements()) {
	        Object key = keys.nextElement();
	        Object value = UIManager.get(key);

	        if (value instanceof FontUIResource) {
	            UIManager.put(key, fontUIResource);
	        }
	    }
	}

	protected void exit() {
	    final Runnable systemExit = new Runnable() {
	        public void run() {
	            System.exit(0);
	        }
	    };

	    final Timer timer = DefaultTimer.getInstance();
	    // after ten seconds, the JVM is shutdown
	    timer.executeAfterDelay(DefaultTimer.seconds(10), systemExit);

	    try {
		    exitImpl();
	    } catch (Exception e) {
	        LogFactory.getLog(getClass()).error("Can't stop properly the Node", e);
	    }

	    systemExit.run();
	}

	protected Action createQuitAction(Resources resources) throws ResourcesException {
		return new QuitAction(resources) {
			protected void exit() {
				SwingApplication.this.exit();
			}
		};
	}



}
