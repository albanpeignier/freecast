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

package org.kolaka.freecast.transport.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;

import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.PeerConnectionFactory;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.test.PeerConnectionFactoryBaseTest;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageWriter;
import org.kolaka.freecast.transport.SocketPeerConnectionFactory;
import org.kolaka.freecast.transport.StreamMessageWriter;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class SocketPeerConnectionFactoryTest extends PeerConnectionFactoryBaseTest {

    private MockSocketFactory mockSocketFactory;
    private MockSocket mockSocket;

    private InetSocketAddress address = new InetSocketAddress(4000);

    protected PeerReference createReference() {
        return InetPeerReference.getInstance(address);
    }

    protected void setUpTestCreate(List sendMessages, List receivedMessages) throws IOException {
        mockSocket.setupGetInputStream(new ByteArrayInputStream(toByteArray(receivedMessages)));
        mockSocket.setupGetOutputStream(new ByteArrayOutputStream());
        
        mockSocket.setExpectedConnectionPoint(address);
    }

    private byte[] toByteArray(List messages) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        MessageWriter writer = new StreamMessageWriter(buffer);
        for (Iterator iter=messages.iterator(); iter.hasNext(); ) {
            Message message = (Message) iter.next();
            writer.write(message);
        }

        byte bytes[] = buffer.toByteArray();

        buffer.close();
        return bytes;
    }

    protected void setUp() throws Exception {
        super.setUp();

        mockSocketFactory = new MockSocketFactory();

        mockSocket = new MockSocket();
        mockSocketFactory.setupCreateSocket(mockSocket);
    }

    protected PeerConnectionFactory createFactory() {
        SocketPeerConnectionFactory factory = new SocketPeerConnectionFactory();
        factory.setSocketFactory(mockSocketFactory);
        return factory;
    }

    protected void verifyTestCreate() {
    }
    
}