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

package org.kolaka.freecast.io.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.kolaka.freecast.io.ReminderInputStream;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ReminderInputStreamTest extends TestCase {

    public void testRead() throws IOException {
        final int length = 1000;
        for (int i = 0; i < length; i++) {
            assertEquals("wrong read byte at " + i, originalBytes[i], input
                    .read());
        }

        testReminderBytes(length);
    }

    public void testReadByteArray() throws IOException {
        final int length = 1000;
        final byte[] buffer = new byte[length];

        int read = input.read(buffer);
        assertEquals("wrong read byte count", length, read);

        for (int i = 0; i < read; i++) {
            assertEquals("wrong read byte at " + i, originalBytes[i], buffer[i]);
        }

        testReminderBytes(length);
    }

    public void testSkip() throws IOException {
        final int length = 1000;

        long skipped = input.skip(length);
        assertEquals("wrong skip byte count", length, skipped);

        testReminderBytes(length);
    }

    protected void testReminderBytes(int length) {
        byte bytes[] = input.toByteArray();

        assertEquals("wrong reminder byte count", length, bytes.length);
        for (int i = 0; i < length; i++) {
            assertEquals("wrong byte at " + i, originalBytes[i], bytes[i]);
        }
    }

    private ReminderInputStream input;

    private byte originalBytes[];

    protected void setUp() throws Exception {
        originalBytes = new byte[1024];
        for (int i = 0; i < originalBytes.length; i++) {
            originalBytes[i] = (byte) (i % Byte.MAX_VALUE);
        }

        input = new ReminderInputStream(new ByteArrayInputStream(originalBytes));
    }

}