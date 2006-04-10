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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.kolaka.freecast.transport.cas.ProtocolMessage.ConnectionMessage;
import org.kolaka.freecast.transport.cas.ProtocolMessage.Registration;
import org.kolaka.freecast.transport.cas.ProtocolMessage.Type;

public class ProtocolCodec implements ProtocolEncoder, ProtocolDecoder {

	public void dispose(IoSession session) throws Exception {

	}

	public void decode(IoSession session, ByteBuffer buffer,
			ProtocolDecoderOutput output) throws Exception {

		int typeValue = buffer.get();
		ProtocolMessage.Type type = ProtocolMessage.Type.getType(typeValue);

		ProtocolMessage message;

		if (type.equals(ProtocolMessage.Registration.TYPE)) {
			InetSocketAddress listenAddress = decodeAddress(buffer);
			message = new ProtocolMessage.Registration(listenAddress);
		} else if (type.equals(ProtocolMessage.ConnectionAssistance.TYPE)) {
			PendingConnection connection = decodeConnection(buffer);
			message = new ProtocolMessage.ConnectionAssistance(connection);
		} else if (type.equals(ProtocolMessage.ConnectionRequest.TYPE)) {
			PendingConnection connection = decodeConnection(buffer);
			message = new ProtocolMessage.ConnectionRequest(connection);
		} else {
			throw new ProtocolDecoderException("Unsupported message type: "
					+ type);
		}

		output.write(message);
	}

	public void encode(IoSession session, Object object,
			ProtocolEncoderOutput output) throws Exception {
		ProtocolMessage message = (ProtocolMessage) object;

		ByteBuffer buffer = ByteBuffer.allocate(1024);

		Type type = message.getType();
		buffer.put((byte) type.getValue());

		if (message instanceof Registration) {
			ProtocolMessage.Registration registrationMessage = (Registration) message;
			encodeAddress(buffer, registrationMessage.getListenAddress());
		} else if (message instanceof ConnectionMessage) {
			ProtocolMessage.ConnectionMessage connectionMessage = (ConnectionMessage) message;
			encodeConnection(buffer, connectionMessage.getPendingConnection());
		} else {
			throw new ProtocolEncoderException("Unsupported message type: "
					+ type);
		}

		buffer.flip();
		output.write(buffer);
	}

	private void encodeAddress(ByteBuffer buffer,
			InetSocketAddress socketAddress) {
		byte[] rawAddress = socketAddress.getAddress().getAddress();

		buffer.put((byte) rawAddress.length);
		buffer.put(rawAddress);
		buffer.putInt(socketAddress.getPort());
	}

	private InetSocketAddress decodeAddress(ByteBuffer buffer)
			throws ProtocolDecoderException {
		int rawAddressLength = buffer.get();
		byte[] rawAddress = new byte[rawAddressLength];
		buffer.get(rawAddress);

		int port = buffer.getInt();

		try {
			return new InetSocketAddress(InetAddress.getByAddress(rawAddress),
					port);
		} catch (UnknownHostException e) {
			throw new ProtocolDecoderException("Can't decode the InetAddress",
					e);
		}
	}

	private PendingConnection decodeConnection(ByteBuffer buffer)
			throws ProtocolDecoderException {
		return new PendingConnection(decodeAddress(buffer),
				decodeAddress(buffer));
	}

	private void encodeConnection(ByteBuffer buffer,
			PendingConnection connection) {
		encodeAddress(buffer, connection.getSourceAddress());
		encodeAddress(buffer, connection.getTargetAddress());
	}

}
