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

package org.kolaka.freecast.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.lang.UnexpectedException;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class DefaultLogicalPage implements LogicalPage {

	private final List packets;

	private final LogicalPageDescriptor descriptor;

	public DefaultLogicalPage(LogicalPageDescriptor descriptor, List packets) {
		Validate.notNull(packets, "No specified packets");
		Validate.notNull(descriptor, "No specified LogicalPageDescriptor");
		Validate.isTrue(descriptor.getCount() == packets.size());

		this.packets = Collections.unmodifiableList(packets);
		this.descriptor = descriptor;
	}

	public boolean isFirstPage() {
		return descriptor.isFirstPage();
	}

	public LogicalPageDescriptor getDescriptor() {
		return descriptor;
	}

	public List packets() {
		return packets;
	}

	private byte[] bytes;

	public byte[] getBytes() {
		if (bytes == null) {
			bytes = createBytes();
		}
		return bytes;
	}

	private byte[] createBytes() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		for (Iterator iter = packets.iterator(); iter.hasNext();) {
			Packet packet = (Packet) iter.next();
			try {
				output.write(packet.getBytes());
			} catch (IOException e) {
				throw new UnexpectedException(
						"IOException into a ByteArrayOutputStream", e);
			}
		}
		return output.toByteArray();
	}

	private int length = -1;

	public int getLength() {
		if (length == -1) {
			length = computeLength();
		}
		return length;
	}

	private int computeLength() {
		int length = 0;
		for (Iterator iter = packets.iterator(); iter.hasNext();) {
			Packet packet = (Packet) iter.next();
			length += packet.getBytes().length;
		}
		return length;
	}

	public boolean isComplete() {
		return true;
	}

	public long getSequenceNumber() {
		return descriptor.getSequenceNumber();
	}

	public long getTimestamp() {
		return descriptor.getTimestamp();
	}

	public boolean equals(Object o) {
		return o instanceof LogicalPage && equals((LogicalPage) o);
	}

	public boolean equals(LogicalPage other) {
		return descriptor.equals(other.getDescriptor());
	}

	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("descriptor", descriptor);
		builder.append("packets.count", packets.size());
		return builder.toString();
	}

	public int hashCode() {
		return descriptor.hashCode();
	}

}
