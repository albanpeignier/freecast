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

package org.kolaka.freecast.tracker.http;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.tracker.NoConfiguredTrackerException;
import org.kolaka.freecast.tracker.statistics.TrackerStatisticsConsumer;
import org.kolaka.freecast.tracker.statistics.TrackerStatisticsConsumerLoader;
import org.kolaka.freecast.transport.cas.ConnectionAssistantServer;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class HttpTrackerConfigurator {

	public void configure(HttpTracker tracker, HierarchicalConfiguration configuration) throws NoConfiguredTrackerException, ConfigurationException {
    /*
     * for the moment, the tracker.class changes the Connector implementation 
     * which creates its own Tracker instance
     */ 
    String trackerClass = configuration.getString("class", "single");
    boolean multiTracker = false; 
    if (trackerClass.equals("multi")) {
      multiTracker = true;
    } else if (trackerClass.equals("none")) {
      throw new NoConfiguredTrackerException();
    }
    tracker.setMultiTracker(multiTracker);
    
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

    List consumerConfigurations = configuration.configurationsAt("statistics.consumer");
    for (Iterator it = consumerConfigurations.iterator(); it.hasNext();) {
      Configuration consumerConfiguration = (Configuration) it.next();
      TrackerStatisticsConsumer consumer = new TrackerStatisticsConsumerLoader().load(consumerConfiguration);
      LogFactory.getLog(getClass()).debug("add consumer : " + consumer);
      tracker.getConsumerManager().add(consumer);
    }
	}

}
