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

package org.kolaka.freecast.transport.cas.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.support.SimpleProtocolDecoderOutput;
import org.apache.mina.filter.codec.support.SimpleProtocolEncoderOutput;
import org.kolaka.freecast.transport.cas.PendingConnection;
import org.kolaka.freecast.transport.cas.ProtocolCodec;
import org.kolaka.freecast.transport.cas.ProtocolMessage;

public class ProtocolCodecTest extends TestCase {

	private ProtocolCodec codec;

	protected void setUp() throws Exception {
		super.setUp();
		codec = new ProtocolCodec();
	}

	public void testEncodeDecode() throws Exception {
		InetSocketAddress localAddress = createAddress(1000);
		InetSocketAddress remoteAddress = createAddress(2000);
		PendingConnection connection = new PendingConnection(localAddress,
				remoteAddress);

		testEncodeDecode(new ProtocolMessage.Registration(localAddress));
		testEncodeDecode(new ProtocolMessage.ConnectionAssistance(connection));
		testEncodeDecode(new ProtocolMessage.ConnectionRequest(connection));
	}

	private InetSocketAddress createAddress(int port)
			throws UnknownHostException {
		return new InetSocketAddress(InetAddress.getLocalHost(), port);
	}

	private void testEncodeDecode(ProtocolMessage message) throws Exception {
		SimpleProtocolEncoderOutput encodeOutput = new SimpleProtocolEncoderOutput() {
			protected WriteFuture doFlush(ByteBuffer buffer) {
				return WriteFuture.newWrittenFuture();
			}
		};
		codec.encode(null, message, encodeOutput);
		encodeOutput.mergeAll();

		ByteBuffer buffer = (ByteBuffer) encodeOutput.getBufferQueue().first();
		System.out.println("message length: " + buffer.remaining());

		SimpleProtocolDecoderOutput decoderOutput = new SimpleProtocolDecoderOutput();
		codec.decode(null, buffer, decoderOutput);

		assertEquals(message, decoderOutput.getMessageQueue().first());
	}

}
