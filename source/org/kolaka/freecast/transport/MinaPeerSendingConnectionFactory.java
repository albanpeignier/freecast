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

package org.kolaka.freecast.transport;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.peer.BasePeerConnectionFactory;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerSendingConnectionFactory;
import org.kolaka.freecast.peer.event.PeerConnectionStatusAdapter;
import org.kolaka.freecast.peer.event.VetoPeerConnectionOpeningException;
import org.kolaka.freecast.service.ControlException;

public class MinaPeerSendingConnectionFactory extends BasePeerConnectionFactory implements PeerSendingConnectionFactory {

	private final IoAcceptor acceptor;
	
	private SocketAddress listenAddress;
	
	public MinaPeerSendingConnectionFactory(final SocketAddress listenAddress) {
		this(listenAddress, new DatagramAcceptor());
	}
	
	public MinaPeerSendingConnectionFactory(final SocketAddress listenAddress, final IoAcceptor acceptor) {
		Validate.notNull(acceptor);
		Validate.notNull(listenAddress);
		this.listenAddress = listenAddress;
		this.acceptor = acceptor;
	}
	
	public void start() throws ControlException {
		LogFactory.getLog(getClass()).debug("bind acceptor on " + listenAddress);
		IoHandler handler = new IoHandlerAdapter() {
			public void messageReceived(IoSession session, Object object) throws Exception {
				Message message = (Message) object;
				LogFactory.getLog(getClass()).debug("receive " + message);
				
				NodeIdentifier identifier = message.getSenderIdentifier();

				MinaPeerSendingConnection connection = (MinaPeerSendingConnection) knownSessions.get(identifier);
				if (connection == null) {
					LogFactory.getLog(getClass()).debug("opening connection with " + identifier);
					try {
						connection = createConnection(session, identifier);
					} catch (VetoPeerConnectionOpeningException e) {
						LogFactory.getLog(getClass()).info("veto on connection from for " + identifier);
						return;
					}
				}
				
				connection.processMessage(message);
			}
			public void exceptionCaught(IoSession session, Throwable t) throws Exception {
				LogFactory.getLog(getClass()).error("exception caught in handler", t);
			}
			public void sessionCreated(IoSession session) throws Exception {
				session.getFilterChain().addFirst("freecast", MinaProtocolCodecFactory.getFilter());
			}
		};
		try {
			acceptor.bind(listenAddress, handler);
		} catch (IOException e) {
			throw new ControlException("Can't bind IoAcceptor on " + listenAddress, e);
		}
	}
	
	public void stop() throws ControlException {
		acceptor.unbindAll();
	}
	
	private Map knownSessions = new TreeMap();

	/**
	 * @param session
	 * @param identifier
	 * @return
	 * @throws VetoPeerConnectionOpeningException
	 */
	private MinaPeerSendingConnection createConnection(IoSession session, final NodeIdentifier identifier) throws VetoPeerConnectionOpeningException {
		MinaPeerSendingConnection connection = new MinaPeerSendingConnection(session);
		connection.setNodeStatusProvider(getStatusProvider());

		fireVetoableConnectionOpening(connection);
		connection.add(new PeerConnectionStatusAdapter() {
			protected void connectionClosed(PeerConnection connection) {
				knownSessions.remove(identifier);
			}
		});
		knownSessions.put(identifier, connection); 
		fireConnectionOpening(connection);
		return connection;
	}
	
}
