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
import java.util.Iterator;
import java.util.List;

import org.easymock.MockControl;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnectionFactory;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageReader;
import org.kolaka.freecast.transport.MessageWriter;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnectionFactoryTest extends PeerConnectionFactoryBaseTest {

	private MockPeerConnection mockConnection;

	private MockControl mockWriterControl;

	private MessageWriter mockWriter;

	private MockControl mockReaderControl;

	private MessageReader mockReader;

	protected void setUp() throws Exception {
		super.setUp();

		mockWriterControl = MockControl.createControl(MessageWriter.class);
		mockWriterControl.setDefaultMatcher(new MessageMatcher());
		mockWriter = (MessageWriter) mockWriterControl.getMock();

		mockReaderControl = MockControl.createControl(MessageReader.class);
		mockReader = (MessageReader) mockReaderControl.getMock();

		mockConnection = new MockPeerConnection(PeerConnection.Type.SOURCE);
		mockConnection.setupCreateReader(mockReader);
		mockConnection.setupCreateWriter(mockWriter);
	}

	protected void setUpTestCreate(List sendMessages, List receivedMessages)
			throws IOException {
		for (Iterator iter = sendMessages.iterator(); iter.hasNext();) {
			Message message = (Message) iter.next();
			mockWriter.write(message);
			mockWriterControl.setReturnValue(1);
		}
		mockWriterControl.replay();

		for (Iterator iter = receivedMessages.iterator(); iter.hasNext();) {
			Message message = (Message) iter.next();
			mockReader.read();
			mockReaderControl.setReturnValue(message);
		}
		mockReaderControl.replay();
	}

	protected void verifyTestCreate() {
		mockReaderControl.verify();
		mockWriterControl.verify();
	}

	protected PeerReference createReference() {
		return InetPeerReference.getInstance("nowhere", 1000, false);
	}

	protected PeerConnectionFactory createFactory() {
		return new PeerConnectionFactory() {
			protected PeerConnection createImpl(Peer peer,
					PeerReference reference) {
				return mockConnection;
			}
		};
	}

}