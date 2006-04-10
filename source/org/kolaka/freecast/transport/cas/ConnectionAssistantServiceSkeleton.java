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

import java.net.InetSocketAddress;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.ConnectionHandler;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.Session;
import org.kolaka.freecast.transport.cas.ProtocolMessage.ConnectionAssistance;
import org.kolaka.freecast.transport.cas.ProtocolMessage.Registration;

public class ConnectionAssistantServiceSkeleton extends IoHandlerAdapter {

	private ConnectionAssistantService service;

	public ConnectionAssistantServiceSkeleton(ConnectionAssistantService service) {
		this.service = service;
	}

	public void sessionCreated(IoSession session) throws Exception {
		session.getFilterChain().addLast("codec",
				ConnectionProtocolCodecFactory.FILTER);
	}

	public void sessionOpened(IoSession session) throws Exception {
		LogFactory.getLog(getClass()).info("sessionOpened");

		Session serviceSession = service.connect();
		session.setAttachment(serviceSession);
	}

	public void sessionClosed(IoSession session) throws Exception {
		Session serviceSession = (Session) session.getAttachment();
		serviceSession.close();
	}

	public void messageReceived(IoSession session, Object object)
			throws Exception {
		ProtocolMessage message = (ProtocolMessage) object;
		LogFactory.getLog(getClass()).info("receive message " + message);

		Session serviceSession = getServiceSession(session);

		if (message instanceof Registration) {
			LogFactory.getLog(getClass()).info("process registration");
			Registration registrationMessage = (Registration) message;
			ConnectionHandler handler = new ConnectionHandlerImpl(session);
			serviceSession.register(registrationMessage.getListenAddress(),
					handler);
		} else if (message instanceof ConnectionAssistance) {
			ConnectionAssistance assistanceMessage = (ConnectionAssistance) message;
			PendingConnection connection = assistanceMessage
					.getPendingConnection();
			serviceSession.assist(connection.getTargetAddress(), connection
					.getSourceAddress());
		} else {
			throw new IllegalArgumentException("Unsupported message: " + message);
		}
	}

	private Session getServiceSession(IoSession session) {
		Session serviceSession = (Session) session.getAttachment();
		Validate.notNull(serviceSession, "Invalid session: " + serviceSession);
		return serviceSession;
	}

	class ConnectionHandlerImpl implements ConnectionHandler {

		private final IoSession session;

		public ConnectionHandlerImpl(IoSession session) {
			this.session = session;
		}

		public void connectionRequested(InetSocketAddress sourceAddress,
				InetSocketAddress targetAddress) {
			PendingConnection connection = new PendingConnection(sourceAddress,
					targetAddress);
			ProtocolMessage message = new ProtocolMessage.ConnectionRequest(
					connection);
			session.write(message);
		}

	}

}
