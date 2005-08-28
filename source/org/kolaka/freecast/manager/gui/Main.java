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

package org.kolaka.freecast.manager.gui;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.NodeConfigurator;
import org.kolaka.freecast.manager.http.HttpServer;
import org.kolaka.freecast.net.InetSocketAddressSpecification;
import org.kolaka.freecast.net.InetSocketAddressSpecificationParser;
import org.kolaka.freecast.net.PublicAddressResolver;
import org.kolaka.freecast.net.SpecificationServerSocketBinder;
import org.kolaka.freecast.node.ConfigurableNode;
import org.kolaka.freecast.node.DefaultNode;
import org.kolaka.freecast.node.Node;
import org.kolaka.freecast.swing.ConfigurableResources;
import org.kolaka.freecast.swing.SwingApplication;
import org.kolaka.freecast.tracker.HttpTracker;
import org.kolaka.freecast.tracker.HttpTrackerConfigurator;

import java.net.InetSocketAddress;
import java.net.InetAddress;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class Main extends SwingApplication {

	private MainFrame frame;
	private HttpServer httpServer;
    private HttpTracker tracker;
    private Node node;
    
    public Main() {
        super("manager");
    }
    
    protected void postInit(Configuration configuration) throws Exception {
		super.postInit(configuration);

		HttpTracker tracker = new HttpTracker();
		new HttpTrackerConfigurator().configure(tracker, configuration.subset("tracker"));
		this.tracker = tracker;

        ConfigurableNode node = new DefaultNode();
		NodeConfigurator nodeConfigurator = new NodeConfigurator();
		nodeConfigurator.setResourceLocator(getResourceLocator());
		nodeConfigurator.configure(node, configuration.subset("node"));
        this.node = node;

		String listenAddressPort = configuration.getString("httpserver.listenaddress.port");
		InetSocketAddressSpecification listenAddressSpecification =
		        new InetSocketAddressSpecificationParser().parse("0.0.0.0", listenAddressPort);
		InetSocketAddress listenAddress = SpecificationServerSocketBinder.select(listenAddressSpecification);

		this.httpServer = new HttpServer(listenAddress);
		InetAddress publicAddress = PublicAddressResolver.getDefaultInstance().getPublicAddress();
		httpServer.setServerName(publicAddress);

		ConfigurableResources resources = new ConfigurableResources(configuration.subset("gui"));
		resources.setResourceLocator(getResourceLocator());

		InetSocketAddress publicHttpServer = new InetSocketAddress(publicAddress, listenAddress.getPort());

		frame = new MainFrame(resources, tracker, node, publicHttpServer);
		frame.setQuitAction(createQuitAction(resources));
		frame.init();
    }

	protected void exitImpl() throws Exception {
     	node.stop();
		node.dispose();

		tracker.stop();
		httpServer.stop();
	}

    protected void run() throws Exception {
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		httpServer.start();

		LogFactory.getLog(Main.class).info("start a HttpTracker on port " + tracker.getListenAddress());
		tracker.start();

		node.init();
		node.start();

		Object lock = new Object();

		synchronized (lock) {
			lock.wait();
		}
    }
    
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

}
