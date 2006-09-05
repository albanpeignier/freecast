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

package org.kolaka.freecast.tracker;

import java.net.InetSocketAddress;

import org.apache.commons.configuration.Configuration;
import org.kolaka.freecast.transport.cas.ConnectionAssistantServer;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class HttpTrackerConfigurator {

	public void configure(HttpTracker tracker, Configuration configuration) {
    /*
     * for the moment, the tracker.class changes the Connector implementation 
     * which creates its own Tracker instance
     */ 
    String trackerClass = configuration.getString("class", "single");
    Class connectorClass = HttpSimpleTrackerConnector.class; 
    if (trackerClass.equals("multi")) {
      connectorClass = HttpMultiTrackerConnector.class;
    }
    tracker.setConnectorClass(connectorClass);
    
		Configuration listenAddressConfiguration = configuration
				.subset("connector.listenaddress");
		InetSocketAddress listenAddress = new InetSocketAddress(
				listenAddressConfiguration.getString("host"),
				listenAddressConfiguration.getInt("port"));
		tracker.setListenAddress(listenAddress);
		
		Configuration casConfiguration = configuration.subset("connection-assistant.listenaddress");
		if (!casConfiguration.isEmpty()) {
			InetSocketAddress casListenAddress = new InetSocketAddress(
					casConfiguration.getString("host"),
					casConfiguration.getInt("port"));
			ConnectionAssistantServer server = new ConnectionAssistantServer();
			server.setListenAddress(casListenAddress);
			tracker.setConnectionAssistantServer(server);
		}
	}

}
