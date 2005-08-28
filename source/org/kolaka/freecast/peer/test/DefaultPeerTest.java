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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.kolaka.freecast.node.DefaultNodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.peer.ConnectivityScoring;
import org.kolaka.freecast.peer.DefaultPeer;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnectionFactoryException;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.transport.MessageReader;
import org.kolaka.freecast.transport.PeerConnectionStatusMessage;
import org.kolaka.freecast.transport.PeerStatusMessage;
import org.kolaka.freecast.transport.test.MockMessageWriter;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultPeerTest extends TestCase {

    public void testUpdatePeerStatus() {
        final PeerStatus initialStatus = new PeerStatus(
                new DefaultNodeIdentifier(0), Order.ZERO);

        DefaultPeer peer = new DefaultPeer(initialStatus);
        assertTrue("wrong initial status", initialStatus.sameAs(peer
                .getStatus()));

        final PeerStatus status = new PeerStatus(new DefaultNodeIdentifier(0),
                Order.ZERO.lower());

        // PropertyChangeListener
        MockControl listenerControl = MockControl
                .createControl(PropertyChangeListener.class);
        PropertyChangeListener listener = (PropertyChangeListener) listenerControl
                .getMock();

        PropertyChangeEvent event = new PropertyChangeEvent(peer,
                Peer.ORDER_PROPERTYNAME, initialStatus.getOrder(), status
                        .getOrder());
        listener.propertyChange(event);
        listenerControl.setMatcher(new PropertyChangeEventMatcher());

        listenerControl.replay();

        peer.add(listener);
        peer.update(status);

        assertTrue("wrong status", status.sameAs(peer.getStatus()));
        listenerControl.verify();
    }

    public void testConnect() throws PeerConnectionFactoryException,
            IOException {
        PeerReference reference = InetPeerReference.getInstance("nowhere",
                1000, true);
        DefaultPeer peer = new DefaultPeer(reference);

        try {
            peer.connect();
            fail("should raise IllegalStateException");
        } catch (IllegalStateException e) {

        }

        MockPeerConnection mockConnection = new MockPeerConnection(
                PeerConnection.Type.SOURCE);

        // NodeStatusProvider
        MockControl statusProviderControl = MockControl
                .createControl(NodeStatusProvider.class);
        NodeStatusProvider statusProvider = (NodeStatusProvider) statusProviderControl
                .getMock();

        statusProvider.getNodeStatus();
        NodeStatus status = new NodeStatus(new DefaultNodeIdentifier(0),
                Order.ZERO);
        statusProviderControl.setReturnValue(status);

        statusProviderControl.replay();

        // MessageWriter/MessageReader
        MockMessageWriter writer = new MockMessageWriter();
        mockConnection.setupCreateWriter(writer);

        MockControl readerControl = MockControl
                .createControl(MessageReader.class);
        MessageReader reader = (MessageReader) readerControl.getMock();

        reader.read();
        readerControl.setReturnValue(new PeerStatusMessage(new PeerStatus(
                new DefaultNodeIdentifier(1), Order.ZERO)));

        reader.read();
        readerControl.setReturnValue(new PeerConnectionStatusMessage(
                PeerConnection.Status.OPENED));

        readerControl.replay();

        mockConnection.setupCreateReader(reader);

        // PeerConnectionFactory
        MockPeerConnectionFactory factory = new MockPeerConnectionFactory();
        factory.setupReturnedConnection(mockConnection);
        factory.setStatusProvider(statusProvider);
        peer.setConnectionFactory(factory);

        // PropertyChangeListener
        MockControl listenerControl = MockControl
                .createControl(PropertyChangeListener.class);
        PropertyChangeListener listener = (PropertyChangeListener) listenerControl
                .getMock();

        listener.propertyChange(new PropertyChangeEvent(peer,
                Peer.ORDER_PROPERTYNAME, Order.UNKNOWN, status.getOrder()));
        listenerControl.setMatcher(new PropertyChangeEventMatcher());
        listener.propertyChange(new PropertyChangeEvent(peer,
                Peer.CONNECTION_PROPERTYNAME, null, mockConnection));

        listenerControl.replay();

        peer.add(listener);

        PeerConnection connection = peer.connect();
        assertEquals("connection should be opened",
                PeerConnection.Status.OPENED, connection.getStatus());
        assertTrue("peer connectivity scoring " + peer.getConnectivityScoring()
                + " should be higher than unknown", peer
                .getConnectivityScoring()
                .compareTo(ConnectivityScoring.UNKNOWN) > 0);
        assertEquals("getConnection should be the same", connection, peer
                .getConnection());
        connection.close();

        assertFalse("peer should be no longer connected", peer.isConnected());
        assertTrue("peer connectivity scoring " + peer.getConnectivityScoring()
                + " should be lower than unknown", peer
                .getConnectivityScoring()
                .compareTo(ConnectivityScoring.UNKNOWN) < 0);

        readerControl.verify();
        factory.verify();
        statusProviderControl.verify();
    }

    /**
     * Needed because the equals method of <code>PropertyChangeEvent</code>
     * isn't as expected.
     * 
     * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
     */
    class PropertyChangeEventMatcher extends AbstractMatcher {
        public boolean matches(Object args1[], Object args2[]) {
            PropertyChangeEvent event1 = (PropertyChangeEvent) args1[0];
            PropertyChangeEvent event2 = (PropertyChangeEvent) args2[0];

            EqualsBuilder builder = new EqualsBuilder();

            builder.append(event1.getSource(), event2.getSource());
            builder.append(event1.getPropertyName(), event2.getPropertyName());
            builder.append(event1.getOldValue(), event2.getOldValue());
            builder.append(event1.getNewValue(), event2.getNewValue());

            return builder.isEquals();
        }

        public String toString(Object args[]) {
            return "event=" + args[0];
        }

    }

}