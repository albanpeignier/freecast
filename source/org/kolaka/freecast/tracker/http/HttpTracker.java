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

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.tracker.DefaultMultiTracker;
import org.kolaka.freecast.tracker.DefaultTracker;
import org.kolaka.freecast.tracker.Main;
import org.kolaka.freecast.tracker.TrackerService;
import org.kolaka.freecast.tracker.statistics.TrackerStatisticsConsumerManager;
import org.kolaka.freecast.tracker.statistics.TrackerStatisticsSetProvider;
import org.kolaka.freecast.transport.cas.ConnectionAssistantServer;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHttpContext;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class HttpTracker implements TrackerService {
  
	private InetSocketAddress listenAddress;

	private Server server;
	private ConnectionAssistantServer caServer;
	
	public void setConnectionAssistantServer(ConnectionAssistantServer caServer) {
		this.caServer = caServer;
	}

	public InetSocketAddress getListenAddress() {
		return listenAddress;
	}

	public void setListenAddress(InetSocketAddress listenAddress) {
		this.listenAddress = listenAddress;
	}

  private TrackerStatisticsConsumerManager consumerManager = new TrackerStatisticsConsumerManager();
  
  public TrackerStatisticsConsumerManager getConsumerManager() {
    return consumerManager;
  }

  private boolean multiTracker;

  private boolean statsServlet;

  public void setMultiTracker(boolean multiTracker) {
    this.multiTracker = multiTracker;
  }
  
	public void start() throws ControlException {
    LogFactory.getLog(Main.class).info(
        "start a HttpConnector on port " + listenAddress);
    
    Class connectorClass = HttpSimpleTrackerConnector.class;
    Object tracker = new DefaultTracker();
    
    if (multiTracker) {
      LogFactory.getLog(Main.class).info("multi network support enabled");

      connectorClass = HttpMultiTrackerConnector.class;
      tracker = new DefaultMultiTracker();
    }
    
    LogFactory.getLog(Main.class).trace(
        "use connector " + connectorClass.getName());

    if (tracker instanceof TrackerStatisticsSetProvider) {
      consumerManager.setProvider((TrackerStatisticsSetProvider) tracker);
    }
    
    server = new Server();
		SocketListener listener = new SocketListener();
		listener.setInetAddress(listenAddress.getAddress());
		listener.setPort(listenAddress.getPort());
		server.addListener(listener);

		ServletHttpContext context = (ServletHttpContext) server
				.getContext("/");

		try {
			ServletHolder servletHolder = context.addServlet("Tracker", "/tracker", connectorClass.getName());
      servletHolder.getServletContext().setAttribute(HttpTrackerConnector.TRACKER_ATTRIBUTE, tracker);
		} catch (Exception e) {
			throw new ControlException("Can't install the tracker servlet", e);
		}

    if (statsServlet) {
      try {
        ServletHolder servletHolder = context.addServlet("Statistics", "/stats.xml", IceStatsServlet.class.getName());
        servletHolder.getServletContext().setAttribute(IceStatsServlet.STATSPROVIDER_ATTRIBUTE, tracker);
      } catch (Exception e) {
        throw new ControlException("Can't install the statistics servlet", e);
      }
    }

		try {
			server.start();
		} catch (Exception e) {
			throw new ControlException("Can't start the http server", e);
		}
		
		if (caServer != null) {
      caServer.start();
		}
    
    consumerManager.start();
	}

	public void stop() throws ControlException {
		try {
			server.stop();
		} catch (InterruptedException e) {
			throw new ControlException("Can't stop the http server", e);
		}

		if (caServer != null) {
			caServer.stop();
		}
    
    consumerManager.stop();
	}

  public void enableStatsServlet(boolean statsServlet) {
    this.statsServlet = statsServlet;
  }

}