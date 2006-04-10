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

package org.kolaka.freecast.transport.receiver.test;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.kolaka.freecast.ogg.DefaultOggPage;
import org.kolaka.freecast.ogg.test.MemoryOggSource;
import org.kolaka.freecast.packet.LogicalPage;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.transport.receiver.DefaultTimedOggPage;
import org.kolaka.freecast.transport.receiver.OggLogicalPageFactory;

public class OggLogicalPageFactoryTest extends TestCase {

	public void testNext() throws IOException {
		int expectedPacketCount = 5;
		byte[] expectedContent = new byte[(int) (Packet.DEFAULT_SIZE
				* expectedPacketCount * 0.9)];
		for (int i = 0; i < expectedContent.length; i++) {
			expectedContent[i] = (byte) (i % Byte.MAX_VALUE);
		}

		final long expectedTimestampDelta = 1000;

		MemoryOggSource source = new MemoryOggSource();

		DefaultOggPage firstOggPage = new DefaultOggPage();
		firstOggPage.setFirstPage(true);
		firstOggPage.setRawBytes(expectedContent);
		source.add(new DefaultTimedOggPage(0, firstOggPage));

		DefaultOggPage secondOggPage = new DefaultOggPage();
		secondOggPage.setAbsoluteGranulePosition(1);
		secondOggPage.setRawBytes(expectedContent);
		source.add(new DefaultTimedOggPage(expectedTimestampDelta,
				secondOggPage));

		OggLogicalPageFactory factory = new OggLogicalPageFactory();
		factory.setSource(source);

		LogicalPage firstLogicalPage = factory.next();
		assertTrue(firstLogicalPage.isFirstPage());
		assertEquals(expectedPacketCount, firstLogicalPage.packets().size());
		assertTrue(Arrays.equals(expectedContent, firstLogicalPage.getBytes()));

		LogicalPage secondLogicalPage = factory.next();
		assertFalse(secondLogicalPage.isFirstPage());
		assertEquals(expectedPacketCount, secondLogicalPage.packets().size());
		assertTrue(Arrays.equals(expectedContent, secondLogicalPage.getBytes()));
		assertEquals(expectedTimestampDelta, secondLogicalPage.getTimestamp()
				- firstLogicalPage.getTimestamp());

		assertTrue(source.isEmpty());
	}

}
