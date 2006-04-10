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

package org.kolaka.freecast.packet.test;

import junit.framework.TestCase;

import org.kolaka.freecast.packet.SequenceGenerator;

public class SequenceGeneratorTest extends TestCase {

	/**
	 * Tests the sequence numbers returned by new <code>SequenceGenerator</code>
	 * instances.
	 * 
	 * @throws InterruptedException
	 *             if the test is in error
	 */
	public void testInitialSequenceNumber() throws InterruptedException {
		long firstSequenceNumber = new SequenceGenerator().next();
		assertTrue(firstSequenceNumber > 0);

		Thread.sleep(1);

		assertTrue(firstSequenceNumber < new SequenceGenerator().next());
	}

	/**
	 * Tests the sequence numbers returned by a <code>SequenceGenerator</code>.
	 */
	public void testSequence() {
		SequenceGenerator generator = new SequenceGenerator();

		long lastSequenceNumber = generator.next();
		for (int i = 0; i < 100; i++) {
			long sequenceNumber = generator.next();
			assertEquals(lastSequenceNumber + 1, sequenceNumber);
			lastSequenceNumber = sequenceNumber;
		}
	}

}
