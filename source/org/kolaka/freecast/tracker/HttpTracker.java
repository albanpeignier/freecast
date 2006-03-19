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
package org.kolaka.freecast.tracker;

import java.net.InetSocketAddress;
import java.util.Set;

import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.transport.cas.ConnectionAssistantServer;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

import com.caucho.hessian.server.HessianServlet;
import com.caucho.services.server.ServiceContext;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class HttpTracker extends HessianServlet implements Tracker {
	private static final long serialVersionUID = 3546076977887720249L;

	private final Tracker tracker;

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

	public void start() throws ControlException {
		server = new Server();
		SocketListener listener = new SocketListener();
		listener.setInetAddress(listenAddress.getAddress());
		listener.setPort(listenAddress.getPort());
		server.addListener(listener);

		ServletHttpContext context = (ServletHttpContext) server
				.getContext("/");

		try {
			context.addServlet("Tracker", "/tracker", HttpTracker.class
					.getName());
		} catch (Exception e) {
			throw new ControlException("Can't install the tracker servlet", e);
		}

		try {
			server.start();
		} catch (Exception e) {
			throw new ControlException("Can't start the http server", e);
		}
		
		if (caServer != null) {
			caServer.start();
		}
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
	}

	public HttpTracker() {
		DefaultTracker.ClientInfoProvider clientInfoProvider = new DefaultTracker.ClientInfoProvider() {
			public String getClientHost() throws TrackerException {
				return ServiceContext.getRequest().getRemoteHost();
			}
		};
		tracker = new DefaultTracker(clientInfoProvider);
	}

	public Set getPeerReferences(NodeIdentifier identifier)
			throws TrackerException {
		return tracker.getPeerReferences(identifier);
	}

	public NodeIdentifier register(PeerReference reference)
			throws TrackerException {
		return tracker.register(reference);
	}

	public void unregister(NodeIdentifier identifier) throws TrackerException {
		tracker.unregister(identifier);
	}

	public void refresh(NodeStatus status) throws TrackerException {
		tracker.refresh(status);
	}

}