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
import org.kolaka.freecast.packet.Checksum;
import org.kolaka.freecast.packet.DefaultLogicalPageDescriptor;
import org.kolaka.freecast.packet.DefaultPacket;
import org.kolaka.freecast.packet.DefaultPacketData;
import org.kolaka.freecast.peer.BasePeerConnection1Factory;
import org.kolaka.freecast.peer.MutablePeer;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnection1;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.PacketMessage;
import org.kolaka.freecast.transport.PeerConnectionStatusMessage;
import org.kolaka.freecast.transport.PeerStatusMessage;

import com.mockobjects.util.Verifier;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class PeerConnectionFactoryBaseTest extends TestCase {

	private MockControl nodeStatusProviderControl;

	private NodeStatusProvider nodeStatusProvider;

	private MockControl peerControl;

	private Peer peer;

	private PeerReference reference;

	protected void setUp() throws Exception {
		reference = createReference();

		nodeStatusProviderControl = MockControl
				.createControl(NodeStatusProvider.class);
		nodeStatusProvider = (NodeStatusProvider) nodeStatusProviderControl
				.getMock();

		peerControl = MockControl.createControl(Peer.class);
		peer = (Peer) peerControl.getMock();
	}

	public final void testCreate() throws Exception {
		// Prepare mock MessageWriter
		NodeStatus localNodeStatus = new NodeStatus(
				new DefaultNodeIdentifier(1), Order.ZERO.lower());

		// Prepare mock MessageReader
		PeerStatus remoteNodeStatus = new PeerStatus(new DefaultNodeIdentifier(
				0), Order.ZERO);

		List sendMessages = new LinkedList();
		sendMessages.add(new PeerStatusMessage(localNodeStatus
				.createPeerStatus()));

		List receivedMessages = new LinkedList();
		receivedMessages.add(new PeerStatusMessage(new PeerStatus(
				new DefaultNodeIdentifier(0), Order.ZERO)));
		receivedMessages.add(new PeerConnectionStatusMessage(
				PeerConnection.Status.OPENED));
		DefaultLogicalPageDescriptor pageDescriptor = new DefaultLogicalPageDescriptor(
				1, 0, 1, true);
		Message message = PacketMessage.getInstance(new DefaultPacket(0, 0,
				new DefaultPacketData(new byte[0]), Checksum.EMPTY,
				pageDescriptor.createElementDescriptor(1)));
		receivedMessages.add(message);

		setUpTestCreate(sendMessages, receivedMessages);

		// Prepare NodeStatusProvider
		nodeStatusProvider.getNodeStatus();
		nodeStatusProviderControl.setReturnValue(localNodeStatus);

		nodeStatusProviderControl.replay();

		// Prepare Peer
		((MutablePeer) peer).update(remoteNodeStatus);
		peerControl.setMatcher(new PeerStatusMatcher());
		peerControl.replay();

		BasePeerConnection1Factory factory = createFactory();
		factory.setStatusProvider(nodeStatusProvider);

		PeerConnection1 connection = factory.create(peer, reference);
		assertEquals("wrong read message", message, connection.getReader()
				.read());

		Verifier.verifyObject(this);
		nodeStatusProviderControl.verify();
		peerControl.verify();

		verifyTestCreate();
	}

	protected abstract void setUpTestCreate(List sendMessages,
			List receivedMessages) throws IOException;

	protected abstract void verifyTestCreate();

	protected abstract PeerReference createReference();

	protected abstract BasePeerConnection1Factory createFactory();

}