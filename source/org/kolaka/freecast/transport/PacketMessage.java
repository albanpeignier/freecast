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

package org.kolaka.freecast.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.kolaka.freecast.packet.Checksum;
import org.kolaka.freecast.packet.DefaultLogicalPageDescriptor;
import org.kolaka.freecast.packet.DefaultPacketData;
import org.kolaka.freecast.packet.LogicalPageDescriptor;
import org.kolaka.freecast.packet.Packet;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PacketMessage extends BaseMessage implements AcknowledgableMessage {

	private Packet packet;

	public PacketMessage() {

	}

	protected PacketMessage(Packet packet) {
		setPacket(packet);
	}

	public Packet getPacket() {
		return packet;
	}
	
	protected void readImpl(DataInputStream input) throws IOException {
		long pageSequenceNumber = input.readLong();
		byte pagePacketCount = input.readByte();
		boolean isFirstPage = input.readBoolean();

		byte packetIndex = input.readByte();

		long packetSequenceNumber = input.readLong();
		long pageTimestamp = input.readLong();

		int packetLength = input.readInt();
		byte packetBytes[] = new byte[packetLength];
		input.readFully(packetBytes);

		Checksum checksum = Checksum.read(input);

		DefaultLogicalPageDescriptor pageDescriptor = new DefaultLogicalPageDescriptor(
				pageSequenceNumber, pageTimestamp, pagePacketCount, isFirstPage);
		LogicalPageDescriptor.Element elementDescriptor = pageDescriptor
				.createElementDescriptor(packetIndex);

		setPacket(new Packet(packetSequenceNumber, pageTimestamp,
				new DefaultPacketData(packetBytes), checksum, elementDescriptor));
	}

	protected void writeImpl(DataOutputStream output) throws IOException {
		LogicalPageDescriptor.Element elementDescriptor = packet
				.getElementDescriptor();
		LogicalPageDescriptor pageDescriptor = elementDescriptor
				.getPageDescriptor();

		output.writeLong(pageDescriptor.getSequenceNumber());
		output.writeByte(pageDescriptor.getCount());
		output.writeBoolean(pageDescriptor.isFirstPage());

		output.writeByte(elementDescriptor.getIndex());

		output.writeLong(packet.getSequenceNumber());
		// TODO to keep protocol unchanged, to be move up
		output.writeLong(pageDescriptor.getTimestamp());
		output.writeInt(packet.getBytes().length);
		output.write(packet.getBytes());
		packet.getChecksum().write(output);
	}

	public boolean equals(Message other) {
		return other instanceof PacketMessage && equals((PacketMessage) other);
	}

	public boolean equals(PacketMessage other) {
		return packet.equals(other.getPacket());
	}

	public int hashCode() {
		return packet.hashCode();
	}

	private void setPacket(Packet packet) {
		this.packet = packet;
	}
	
	public boolean isImportant() {
		return packet.getElementDescriptor().getPageDescriptor().isFirstPage();
	}

	public int getAcknowledgmentIdentifier() {
		return (int) (packet.getSequenceNumber() % Integer.MAX_VALUE);
	}

	public static PacketMessage getInstance(Packet packet) {
		return new PacketMessage(packet);
	}

	public MessageType getType() {
		return MessageType.PACKET;
	}

}