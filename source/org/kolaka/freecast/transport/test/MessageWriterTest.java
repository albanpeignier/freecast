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

package org.kolaka.freecast.transport.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.NotImplementedException;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageType;
import org.kolaka.freecast.transport.StreamMessageWriter;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class MessageWriterTest extends TestCase {
    public void testWrite() throws IOException, InterruptedException {
        final int messageLength = 1024;
        StreamMessageWriter writer = new StreamMessageWriter(
                new NullOutputStream());

        assertEquals("initial bandwith not null", 0, writer.getBandwith());

        Message message = new Message() {
            private final byte[] buffer = new byte[messageLength];

            public MessageType getType() {
                return MessageType.PACKET;
            }

            public void write(DataOutputStream output) throws IOException {
                output.write(buffer);
            }

            public void read(DataInputStream input) throws IOException {
                throw new NotImplementedException(getClass());
            }
        };
        writer.write(message);

        final int writeLength = (messageLength + 4);
        final int expectedBandwidth = writeLength * 8;
        assertEquals("first bandwith wrong", expectedBandwidth, writer
                .getBandwith());

        Thread.sleep(1000);

        for (int messageCount = 0; messageCount < 40; messageCount++) {
            writer.write(message);
            Thread.sleep(1000);

            int bandwidth = writer.getBandwith();
            long bandwidthError = expectedBandwidth - bandwidth;
            assertTrue("bandwith wrong (" + bandwidth + "/" + expectedBandwidth
                    + ")", bandwidthError < (expectedBandwidth * 0.05));
        }

        assertEquals("wrong average length", writeLength, writer
                .getAverageLength());
    }
}