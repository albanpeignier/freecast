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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class StreamMessageWriter implements MessageWriter {

	private final DataOutputStream output;

	private final CountingOutputStream countingOutput;

	public synchronized int write(Message message) throws IOException {
		if (message == null) {
			throw new IllegalArgumentException("No specified message");
		}

		int before = countingOutput.getCount();
		output.write(Message.CAPTURE_PATTERN);
		output.writeByte(message.getType().getValue());
		message.write(output);
		int length = countingOutput.getCount() - before;

		addWrite(length);

		output.flush();

		return length;
	}

	protected void addWrite(int length) {
		writes.add(new Write(length));
		trim();
	}

	protected void trim() {
		long now = System.currentTimeMillis();

		for (Iterator iter = writes.iterator(); iter.hasNext();) {
			Write current = (Write) iter.next();
			long age = current.getAge(now);
			if (age > maximumWriteAge) {
				iter.remove();
			} else {
				break;
			}
		}
	}

	public int getBandwith() {
		return getBandwidth(maximumWriteAge);
	}

	protected int getBandwidth(long interval) {
		trim();

		if (writes.isEmpty()) {
			return 0;
		}

		Write oldest = (Write) writes.get(0);
		if (writes.size() == 1) {
			return oldest.getLength() * 8;
		}

		long timeLength = oldest.getAge();
		long writeLength = getWriteLength(interval);

		double secondTimeLength = timeLength / 1000.0;
		long bitWriteLength = writeLength * 8;

		return (int) (bitWriteLength / secondTimeLength);
	}

	public int getWriteLength() {
		return getWriteLength(maximumWriteAge);
	}

	public int getWriteLength(long timeLength) {
		long oldest = System.currentTimeMillis() - timeLength;

		int writeLength = 0;
		for (Iterator iterator = writes.iterator(); iterator.hasNext();) {
			Write current = (Write) iterator.next();
			if (current.getTimestamp() > oldest) {
				writeLength += current.getLength();
			}
		}
		return writeLength;
	}

	public int getAverageLength() {
		trim();
		return getWriteLength() / writes.size();
	}

	public StreamMessageWriter(final OutputStream output) {
		this.countingOutput = new CountingOutputStream(output);
		this.output = new DataOutputStream(countingOutput);
	}

	private long maximumWriteAge = 30 * 1000;

	private List writes = new LinkedList();

	static class Write {

		private final long timestamp;

		private final int length;

		public Write(int length) {
			this.timestamp = System.currentTimeMillis();
			this.length = length;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public int getLength() {
			return length;
		}

		public long getAge() {
			return getAge(System.currentTimeMillis());
		}

		public long getAge(long now) {
			return now - timestamp;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}

	public String toString() {
		return new ToStringBuilder(this).append("bandwidth", getBandwith())
				.toString();
	}

}
