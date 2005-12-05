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

package org.kolaka.freecast.packet;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.packet.LogicalPageDescriptor.Element;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class DefaultPacket implements Packet {

    private final long sequenceNumber;
    private final byte[] bytes;
    private final Checksum checksum;
    private final LogicalPageDescriptor.Element elementDescriptor;

    public DefaultPacket(long sequenceNumber, long timestamp, PacketData packetData, Checksum checksum, Element elementDescriptor) {
        Validate.notNull(packetData, "No specified PacketData");
        Validate.notNull(checksum, "No specified Checksum");
        Validate.notNull(elementDescriptor, "No specified LogicalPageDescriptor.Element");
        
        this.sequenceNumber = sequenceNumber;
        this.bytes = packetData.getBytes();
        this.checksum = checksum;
        this.elementDescriptor = elementDescriptor;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public Element getElementDescriptor() {
        return elementDescriptor;
    }

    public boolean equals(Object o) {
        return o instanceof DefaultPacket && equals((DefaultPacket) o);
    }
    
    public boolean equals(DefaultPacket other) {
        return other != null && sequenceNumber == other.sequenceNumber;
    }
    
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("sequenceNumber", sequenceNumber);
        builder.append("bytes.length", bytes.length);
        builder.append("checksum", checksum);
        builder.append("elementDescriptor", elementDescriptor);
        return builder.toString();
    }

}
