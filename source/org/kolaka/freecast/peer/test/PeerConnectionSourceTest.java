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

package org.kolaka.freecast.peer.test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.kolaka.freecast.node.DefaultNodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnectionSource;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningListener;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionOpeningListener;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageReader;
import org.kolaka.freecast.transport.MessageWriter;
import org.kolaka.freecast.transport.PeerConnectionStatusMessage;
import org.kolaka.freecast.transport.PeerStatusMessage;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnectionSourceTest extends TestCase {

	public void testAccept() throws Exception {
		NodeStatus status = new NodeStatus(new DefaultNodeIdentifier(1),
				Order.ZERO);
		PeerStatus peerStatus = new PeerStatus(new DefaultNodeIdentifier(2),
				Order.ZERO.lower());

		// NodeStatusProvider
		MockControl statusProviderControl = MockControl
				.createControl(NodeStatusProvider.class);
		NodeStatusProvider statusProvider = (NodeStatusProvider) statusProviderControl
				.getMock();
		statusProvider.getNodeStatus();
		statusProviderControl.setReturnValue(status);
		statusProviderControl.replay();

		// PeerConnection
		MockPeerConnection connection = new MockPeerConnection(
				PeerConnection.Type.RELAY);

		MockControl writerControl = MockControl
				.createControl(MessageWriter.class);
		writerControl.setDefaultMatcher(new MessageMatcher());
		MessageWriter writer = (MessageWriter) writerControl.getMock();
		writer.write(new PeerStatusMessage(status.createPeerStatus()));
		writerControl.setReturnValue(1);

		PeerConnectionStatusMessage connectionStatusMessage = new PeerConnectionStatusMessage(
				PeerConnection.Status.OPENED);
		writer.write(connectionStatusMessage);
		writerControl.setReturnValue(1);

		writerControl.replay();

		connection.setupCreateWriter(writer);

		// TODO the mock read throws a exception after the second message
		MockMessageReader reader = new MockMessageReader();
		reader.add(new PeerStatusMessage(peerStatus));
		reader.add(connectionStatusMessage);

		connection.setupCreateReader(reader);

		// Listeners
		MockControl listenerControl = MockControl
				.createControl(PeerConnectionOpeningListener.class);
		PeerConnectionOpeningListener listener = (PeerConnectionOpeningListener) listenerControl
				.getMock();

		listener.connectionOpening(connection);
		listenerControl.replay();

		MockControl vetoableListenerControl = MockControl
				.createControl(VetoablePeerConnectionOpeningListener.class);
		VetoablePeerConnectionOpeningListener vetoableListener = (VetoablePeerConnectionOpeningListener) vetoableListenerControl
				.getMock();

		vetoableListener.vetoableConnectionOpening(connection);
		vetoableListenerControl.replay();

		// Registry
		MockControl registryControl = MockControl
				.createControl(PeerConnectionSource.Registry.class);
		PeerConnectionSource.Registry registry = (PeerConnectionSource.Registry) registryControl
				.getMock();

		registry.registry(connection);
		registryControl.replay();

		TestPeerConnectionSource source = new TestPeerConnectionSource();
		source.setStatusProvider(statusProvider);
		source.add(listener);
		source.add(vetoableListener);
		source.setRegistry(registry);

		assertEquals("wrong intial connection Status",
				PeerConnection.Status.OPENING, connection.getStatus());

		source.accept(connection);

		Thread.sleep(100);

		assertEquals("wrong connection PeerStatus", peerStatus, connection
				.getLastPeerStatus());
		assertEquals("wrong final connection Status",
				PeerConnection.Status.OPENED, connection.getStatus());

		statusProviderControl.verify();
		assertTrue(reader.isEmpty());
		writerControl.verify();

		listenerControl.verify();
		vetoableListenerControl.verify();

		// shutdown the PeerConnection
		reader
				.add(new PeerConnectionStatusMessage(
						PeerConnection.Status.CLOSED));
	}

	class TestPeerConnectionSource extends PeerConnectionSource {

		public void accept(PeerConnection connection) {
			super.accept(connection);
		}

		public void start() {

		}

		public void stop() {

		}

	}

	class MockMessageReader implements MessageReader {

		private List messages = new LinkedList();

		public Message read() throws IOException {
			synchronized (messages) {
				while (messages.isEmpty()) {
					try {
						messages.wait();
					} catch (InterruptedException e) {
						throw new IOException("Can't wait the next message");
					}
				}
			}

			return (Message) messages.remove(0);
		}

		public void add(Message message) {
			synchronized (messages) {
				messages.add(message);
				messages.notifyAll();
			}
		}

		public boolean isEmpty() {
			return messages.isEmpty();
		}

	}

}