/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2006 Alban Peignier
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

package org.kolaka.freecast.manager.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;
import org.jdesktop.jdic.desktop.Message;
import org.kolaka.freecast.swing.BaseAction;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class EmailHomepageAction extends BaseAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6845487445187546115L;

	private final InetSocketAddress publicHttpServer;

	public EmailHomepageAction(Resources resources,
			InetSocketAddress publicHttpServer) throws ResourcesException {
		super("Email network homepage");
		this.publicHttpServer = publicHttpServer;
		loadIcons(resources, "visit.email");
	}

	public void actionPerformed(ActionEvent event) {
		try {
			sendMail();
		} catch (Exception e) {
			LogFactory.getLog(getClass()).error(
					"can't email to visit " + publicHttpServer, e);
		}
	}
	
	private void sendMail() throws IOException, DesktopException {
		URL url = new URL("http", publicHttpServer.getHostName(), publicHttpServer.getPort(), "/");
		LogFactory.getLog(getClass()).debug("email " + url);

		final Message message = new Message();
		message.setSubject("My FreeCast Network");
		message
				.setBody("Visit the homepage of my FreeCast network: "
						+ url);

		Desktop.mail(message);
	}

}
