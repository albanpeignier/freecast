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

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.ConfigurableNode;
import org.kolaka.freecast.swing.BaseAction;
import org.kolaka.freecast.swing.ErrorPane;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;
import org.kolaka.freecast.transport.receiver.Receiver;
import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ReceiverConfigurator;
import org.kolaka.freecast.transport.receiver.ReceiverControler;
import org.kolaka.freecast.transport.receiver.StaticReceiverControler;

public class SetupAction extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6050873549840760116L;
	private final JFrame parent;
	private final Resources resources;
	private final ConfigurableNode node;
	
	public SetupAction(Resources resources, JFrame parent, ConfigurableNode node) throws ResourcesException {
		super("Configure FreeCast");
		this.parent = parent;
		this.resources = resources;
		this.node = node;
		loadIcons(resources, "main");
	}
	
	private SetupDialog dialog;

	public void actionPerformed(ActionEvent event) {
		if (dialog != null && dialog.isVisible()) {
			dialog.toFront();
			return;
		}
		
		try {
			dialog = new SetupDialog(resources, parent);
		} catch (ResourcesException exception) {
			LogFactory.getLog(getClass()).error("Can't create setup dialog",exception);
			return;
		}
		
		dialog.setReceiverConfiguration(node.getReceiverControler().getReceiverConfiguration());
		
		dialog.getReceiverConfiguration().addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				ReceiverConfiguration configuration = (ReceiverConfiguration) arg;
				LogFactory.getLog(getClass()).debug("configuration returns by setup dialog: " + configuration);
				
				if (configuration == null) {
					return;
				}
				
				if (configuration.equals(node.getReceiverControler().getReceiverConfiguration())) {
					LogFactory.getLog(getClass()).debug("configuration not changed");
					return;
				}
				
				try {
					ReceiverControler receiverControler = node.getReceiverControler();
					receiverControler.stop();

					ReceiverConfigurator configurator = ReceiverConfigurator.getInstance(configuration);
					Receiver receiver = configurator.configure(configuration);

					LogFactory.getLog(getClass()).debug("new receiver: " + receiver);

					receiverControler = new StaticReceiverControler(receiver);
					
					node.setReceiverControler(receiverControler);
					
					receiverControler.start();
				} catch (Throwable cause) {
					new ErrorPane(parent).show("Can't change FreeCast setup", cause);
				}
			}
		});
		
		dialog.setVisible(true);
	}

}
