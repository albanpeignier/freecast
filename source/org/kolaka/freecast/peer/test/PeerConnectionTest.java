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

import java.io.EOFException;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnection1;
import org.kolaka.freecast.transport.MessageReader;
import org.kolaka.freecast.transport.PeerConnectionStatusMessage;
import org.kolaka.freecast.transport.test.MockMessageWriter;

import com.mockobjects.util.Verifier;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnectionTest extends TestCase {

	private MockPeerConnection connection;

	protected void setUp() {
		connection = new MockPeerConnection(PeerConnection1.Type.SOURCE);
	}

	public void testOpen() throws InterruptedException {
		MockMessageWriter writer = new MockMessageWriter();
		connection.setupCreateWriter(writer);

		assertEquals("status should be OPENING", PeerConnection.Status.OPENING,
				connection.getStatus());
		connection.open();
		assertEquals("status should be OPENED", PeerConnection.Status.OPENED,
				connection.getStatus());

		Thread.sleep(100);

		List messages = writer.getWritedMessages();

		assertEquals("one message should be writed", 1, messages.size());
		assertTrue("message should be a PeerStatusMessage",
				messages.get(0) instanceof PeerConnectionStatusMessage);
		PeerConnection.Status sendStatus = ((PeerConnectionStatusMessage) messages
				.get(0)).getStatus();
		assertEquals("PeerConnection.Status should OPENED",
				PeerConnection.Status.OPENED, sendStatus);

		Verifier.verifyObject(this);
	}

	public void testClose() throws InterruptedException {
		MockMessageWriter writer = new MockMessageWriter();
		connection.setupCreateWriter(writer);

		connection.open();

		connection.close();
		assertEquals("status should be CLOSED", PeerConnection.Status.CLOSED,
				connection.getStatus());

		Thread.sleep(200);

		List messages = writer.getWritedMessages();
		PeerConnectionStatusMessage statusMessage = (PeerConnectionStatusMessage) messages
				.get(messages.size() - 1);

		assertEquals("status should be CLOSED", PeerConnection.Status.CLOSED,
				statusMessage.getStatus());

		Verifier.verifyObject(this);
	}

	public void testErrorWhenClosed() throws IOException {
		MockMessageWriter writer = new MockMessageWriter();
		connection.setupCreateWriter(writer);

		MockControl readerControl = MockControl
				.createControl(MessageReader.class);
		MessageReader reader = (MessageReader) readerControl.getMock();

		connection.setupCreateReader(reader);
		connection.setupCreateWriter(writer);

		connection.close();

		try {
			connection.getReader().read();
			fail("should raise an EOFException");
		} catch (EOFException e) {

		}

		Verifier.verifyObject(this);
	}

	public void testErrorInReader() throws IOException {
		MockMessageWriter writer = new MockMessageWriter();
		connection.setupCreateWriter(writer);

		MockControl readerControl = MockControl
				.createControl(MessageReader.class);
		MessageReader reader = (MessageReader) readerControl.getMock();

		reader.read();
		readerControl.setThrowable(new IOException());

		readerControl.replay();

		connection.setupCreateReader(reader);
		connection.setupCreateWriter(writer);

		connection.open();

		try {
			connection.getReader().read();
			fail("should raise an IOException");
		} catch (IOException e) {

		}

		assertEquals("status should be CLOSED", PeerConnection.Status.CLOSED,
				connection.getStatus());

		Verifier.verifyObject(this);
	}

	/*
	 * public void testErrorInWriter() { MockMessageWriter writer = new
	 * MockMessageWriter(); connection.setupCreateWriter(writer);
	 * 
	 * connection.open();
	 * 
	 * writer.setThrowable(new IOException()); try {
	 * connection.getWriter().write(FakeMessages.createMessage()); fail("should
	 * raise an IOException"); } catch (IOException e) {
	 *  }
	 * 
	 * assertEquals("status should be CLOSED", PeerConnection.Status.CLOSED,
	 * connection.getStatus());
	 * 
	 * Verifier.verifyObject(this); }
	 */

}