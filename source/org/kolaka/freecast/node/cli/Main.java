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

package org.kolaka.freecast.node.cli;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.Application;
import org.kolaka.freecast.NodeConfigurator;
import org.kolaka.freecast.config.Configurations;
import org.kolaka.freecast.node.ConfigurableNode;
import org.kolaka.freecast.node.DefaultNode;
import org.kolaka.freecast.node.Node;
import org.kolaka.freecast.service.ControlException;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 * @todo share code between the various Main
 */
public class Main extends Application {
	private Node node;

	public Main() {
		super("node-cli");
	}

	public static void main(String args[]) {
		new Main().run(args);
	}

	protected void postInit(HierarchicalConfiguration configuration) throws Exception {
		ConfigurableNode node = new DefaultNode();
		NodeConfigurator nodeConfigurator = new NodeConfigurator();
		nodeConfigurator.setResourceLocator(getResourceLocator());
		nodeConfigurator.configure(node, Configurations.subset(configuration, "node"));
		this.node = node;
	}

	protected void run() throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					node.stop();
					node.dispose();
				} catch (ControlException e) {
					LogFactory.getLog(getClass()).error(
							"Can't stop properly the Node", e);
				}
			}
		});

		node.init();
		node.start();

		Object lock = new Object();

		synchronized (lock) {
			lock.wait();
		}
	}

}