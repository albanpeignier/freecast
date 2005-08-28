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

package org.kolaka.freecast.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.kolaka.freecast.packet.Checksum;
import org.kolaka.freecast.packet.DefaultLogicalPageDescriptor;
import org.kolaka.freecast.packet.DefaultPacket;
import org.kolaka.freecast.packet.DefaultPacketData;
import org.kolaka.freecast.packet.LogicalPageDescriptor;
import org.kolaka.freecast.packet.Packet;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PacketMessage extends BaseMessage {

    private Packet packet;

    public PacketMessage() {

    }

    protected PacketMessage(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public void read(DataInputStream input) throws IOException {
        DefaultLogicalPageDescriptor pageDescriptor = 
            new DefaultLogicalPageDescriptor(input.readLong(), input.readByte(), input.readBoolean());
        LogicalPageDescriptor.Element elementDescriptor =
            pageDescriptor.createElementDescriptor(input.readByte());
        
        long sequenceNumber = input.readLong();
        long timestamp = input.readLong();

        int length = input.readInt();
        byte bytes[] = new byte[length];
        input.readFully(bytes);
        
        Checksum checksum = Checksum.read(input);
        
        packet = 
            new DefaultPacket(sequenceNumber, timestamp, new DefaultPacketData(bytes), checksum, elementDescriptor); 
    }

    public void write(DataOutputStream output) throws IOException {
        LogicalPageDescriptor.Element elementDescriptor = packet.getElementDescriptor();
        LogicalPageDescriptor pageDescriptor = elementDescriptor.getPageDescriptor();   
            
        output.writeLong(pageDescriptor.getSequenceNumber());
        output.writeByte(pageDescriptor.getCount());
        output.writeBoolean(pageDescriptor.isFirstPage());
        
        output.writeByte(elementDescriptor.getIndex());
        
        output.writeLong(packet.getSequenceNumber());
        output.writeLong(packet.getTimestamp());
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

    protected void setPacket(Packet packet) {
        this.packet = packet;
    }

    public static PacketMessage getInstance(Packet packet) {
        return new PacketMessage(packet);
    }

    public MessageType getType() {
        return MessageType.PACKET;
    }

}