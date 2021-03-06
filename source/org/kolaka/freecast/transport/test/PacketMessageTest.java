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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.packet.test.PacketGenerator;
import org.kolaka.freecast.transport.PacketMessage;

public class PacketMessageTest extends TestCase {

	public void testWriteRead() throws IOException {
		Packet packet = new PacketGenerator().generate();

		PacketMessage message = PacketMessage.getInstance(packet);
    message.setSenderIdentifier(new NodeIdentifier(0));

		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(
				byteOutputStream);
		message.write(dataOutputStream);
		dataOutputStream.close();

		byte[] packetRawBytes = byteOutputStream.toByteArray();

		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
				packetRawBytes);
		PacketMessage readMessage = new PacketMessage();
		DataInputStream dataInputStream = new DataInputStream(byteInputStream);
		readMessage.read(dataInputStream);
		dataInputStream.close();

		Packet readPacket = readMessage.getPacket();
		assertEquals(packet.getSequenceNumber(), readPacket.getSequenceNumber());
		assertEquals(packet.getChecksum(), readPacket.getChecksum());
	}

}
