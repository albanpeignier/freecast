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

package org.kolaka.freecast.transport.cas;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.kolaka.freecast.transport.cas.ProtocolMessage.ConnectionRequest;

public class ConnectionAssistantServiceStub implements
		ConnectionAssistantService {

	private final SocketAddress serviceAddress;

	private final IoConnector connector;

	public ConnectionAssistantServiceStub(IoConnector connector,
			SocketAddress serviceAddress) {
		this.connector = connector;
		this.serviceAddress = serviceAddress;
	}

	public Session connect() throws Exception {
		LogFactory.getLog(getClass()).info("new session");

		SessionImpl session = new SessionImpl();

		IoSession protocolSession;

		try {
			ConnectFuture connectFuture = connector.connect(serviceAddress,
					session.getProtocolHandler());
			connectFuture.join();
			protocolSession = connectFuture.getSession();
		} catch (IOException e) {
			throw new Exception("Can't connection to " + serviceAddress, e);
		}

		session.setProtocolSession(protocolSession);

		return session;
	}

	public class SessionImpl implements Session {

		public void assist(InetSocketAddress remoteAddress,
				InetSocketAddress localAddress) throws Exception {
			PendingConnection connection = new PendingConnection(localAddress,
					remoteAddress);
			protocolSession.write(new ProtocolMessage.ConnectionAssistance(
					connection));
		}

		private IoSession protocolSession;

		public void setProtocolSession(IoSession protocolSession) {
			this.protocolSession = protocolSession;
		}

		public IoHandler getProtocolHandler() {
			return new IoHandlerAdapter() {
				public void sessionCreated(IoSession session)
						throws java.lang.Exception {
					session.getFilterChain().addLast("codec",
							ConnectionProtocolCodecFactory.FILTER);

				}

				public void messageReceived(IoSession session, Object message)
						throws Exception {
					ConnectionRequest request = (ConnectionRequest) message;
					processConnectionRequest(request.getPendingConnection());
				}

				public void exceptionCaught(IoSession session, Throwable t)
						throws Exception {
					LogFactory.getLog(getClass()).error(t);
				}
			};
		}

		void processConnectionRequest(PendingConnection connection) {
			ConnectionHandler handler = (ConnectionHandler) handlers
					.get(connection.getTargetAddress());
			if (handler == null) {
				throw new NotImplementedException("Invalid connection request "
						+ connection);
			}
			handler.connectionRequested(connection.getSourceAddress(),
					connection.getTargetAddress());
		}

		public void close() throws Exception {
			LogFactory.getLog(getClass()).info("close session");
			protocolSession.close();
			handlers.clear();
		}

		private final Map handlers = new HashMap();

		public void register(InetSocketAddress localAddress,
				ConnectionHandler handler) throws Exception {
			protocolSession
					.write(new ProtocolMessage.Registration(localAddress));
			handlers.put(localAddress, handler);
		}

	}

}
