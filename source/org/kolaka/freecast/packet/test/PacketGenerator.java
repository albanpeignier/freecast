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

package org.kolaka.freecast.packet.test;

import org.kolaka.freecast.packet.Checksum;
import org.kolaka.freecast.packet.DefaultLogicalPageDescriptor;
import org.kolaka.freecast.packet.DefaultPacket;
import org.kolaka.freecast.packet.DefaultPacketData;
import org.kolaka.freecast.packet.LogicalPageDescriptor;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.packet.SequenceGenerator;

public class PacketGenerator {

	private SequenceGenerator sequenceGenerator = new SequenceGenerator();
	
	public Packet generate() {
		byte[] expectedBytes = new byte[Packet.DEFAULT_SIZE];
		for (int i = 0; i < expectedBytes.length; i++) {
			expectedBytes[i] = (byte) (i % Byte.MAX_VALUE);
		}

		byte[] expectedChecksumContent = new byte[64];
		for (int i = 0; i < expectedChecksumContent.length; i++) {
			expectedChecksumContent[i] = (byte) (i % Byte.MAX_VALUE);
		}
		Checksum checksum = new Checksum(expectedChecksumContent);

		DefaultLogicalPageDescriptor pageDescriptor = new DefaultLogicalPageDescriptor(
				1, 0, 1, true);
		LogicalPageDescriptor.Element elementDescriptor = pageDescriptor
				.createElementDescriptor(1);

		return new DefaultPacket(sequenceGenerator.next(), System.currentTimeMillis(),
				new DefaultPacketData(expectedBytes), checksum,
				elementDescriptor);
	}
	
}
