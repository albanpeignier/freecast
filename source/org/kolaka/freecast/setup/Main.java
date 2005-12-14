/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004 Alban Peignier
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

package org.kolaka.freecast.setup;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ClassUtils;
import org.kolaka.freecast.lang.mutable.ObservableValue;
import org.kolaka.freecast.swing.ConfigurableResources;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.SwingApplication;

public class Main extends SwingApplication {
	
	private SetupDialog dialog;

	public Main() {
		super("setup");
	}
	
	protected void postInit(Configuration configuration) throws Exception {
		super.postInit(configuration);
		Resources resources = new ConfigurableResources(configuration.subset("gui.setup"));
		dialog = new SetupDialog(resources, null);
	}
	
	protected void run() throws Exception {
		dialog.setVisible(true);
		
		ObservableValue configuration = dialog.getReceiverConfiguration();
		
		configuration.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				System.out.println(arg);
				exit();
			}
		});
		
		Object lock = new Object();

		synchronized (lock) {
			lock.wait();
		}
	}
	
	protected void exitImpl() throws Exception {
		
	}
	
	public static void main(String[] args) throws Exception {
		new Main().run(args);
	}

}
