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

package org.kolaka.freecast.transport.test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.logging.LogFactory;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.apache.mina.util.AvailablePortFinder;
import org.kolaka.freecast.node.test.MockNodeStatusProvider;
import org.kolaka.freecast.packet.test.PacketGenerator;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningListener;
import org.kolaka.freecast.peer.event.PeerConnectionStatusAdapter;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.test.BaseTestCase;
import org.kolaka.freecast.transport.BaseMinaPeerConnection;
import org.kolaka.freecast.transport.MinaPeerReceivingConnection;
import org.kolaka.freecast.transport.MinaPeerSendingConnectionFactory;
import org.kolaka.freecast.transport.PacketMessage;

public class MinaPeerReceivingConnectionTest extends BaseTestCase {
	
	public void testOpen() throws Exception {
		int port = AvailablePortFinder.getNextAvailable(1024);
		SocketAddress address = new InetSocketAddress(port);
		
		DatagramAcceptor acceptor = new DatagramAcceptor();
		MinaPeerSendingConnectionFactory connectionFactory = new MinaPeerSendingConnectionFactory(address, acceptor);
		connectionFactory.setStatusProvider(new MockNodeStatusProvider());
		connectionFactory.add(new FakeSender());
		connectionFactory.start();
		
		Thread.sleep(1000);
		
		DatagramConnector connector = new DatagramConnector();
		MinaPeerReceivingConnection connection = new MinaPeerReceivingConnection(address, connector);

		/* TODO fixe the strange problem with the mock
		// PeerConnectionStatusListener mock
		MockControl listenerControl = MockControl.createControl(PeerConnectionStatusListener.class);
		PeerConnectionStatusListener listener = (PeerConnectionStatusListener) listenerControl.getMock();
		
		listener.peerConnectionStatusChanged(new PeerConnectionStatusEvent(connection, PeerConnection.Status.OPENING));
		listener.peerConnectionStatusChanged(new PeerConnectionStatusEvent(connection, PeerConnection.Status.OPENED));
		listener.peerConnectionStatusChanged(new PeerConnectionStatusEvent(connection, PeerConnection.Status.ACTIVATED));
		listener.peerConnectionStatusChanged(new PeerConnectionStatusEvent(connection, PeerConnection.Status.CLOSED));
		
		listenerControl.replay();
		connection.add(listener);
		*/
		
		PeerConnectionStatusListener logListener = new PeerConnectionStatusListener() {
			public void peerConnectionStatusChanged(PeerConnectionStatusEvent event) {
				LogFactory.getLog(getClass()).debug("event received: " + event);
			}
		};
		connection.add(logListener);

		connection.setNodeStatusProvider(new MockNodeStatusProvider());
		
		connection.open();

		Thread.sleep(1000);

		connection.activate();

		Thread.sleep(BaseMinaPeerConnection.PING_DELAY);

		assertTrue(connection.getLatency() < 500);
		
		connection.close();

		Thread.sleep(1000);

		connectionFactory.stop();

		// listenerControl.verify();
	}

	class FakeSender implements PeerConnectionOpeningListener, Runnable {
		
		private PeerConnection connection;
		private PacketGenerator generator = new PacketGenerator();
		
		public void connectionOpening(PeerConnection connection) {
			this.connection = connection;
			PeerConnectionStatusListener listener = new PeerConnectionStatusAdapter() {
				protected void connectionActivated(PeerConnection connection) {
					new Thread(FakeSender.this).start();
				}
			};
			connection.add(listener);
		}
		
		public void run() {
			try {
				while (!connection.getStatus().equals(PeerConnection.Status.CLOSED)) {
						Thread.sleep(200);
						connection.getWriter().write(PacketMessage.getInstance(generator.generate()));
				}
			} catch (Exception e) {
				LogFactory.getLog(getClass()).error("can't send packet", e);
				return;
			}
		}
		
	}
	
}
