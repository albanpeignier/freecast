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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.Validate;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class Checksum {

	public static final Checksum EMPTY = new Checksum(new byte[0]);

	private byte[] data;

	public Checksum(byte[] data) {
		Validate.notNull(data);
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public static Checksum read(DataInputStream input) throws IOException {
		int length = input.readInt();

		byte bytes[] = new byte[length];
		if (length > 0) {
			input.readFully(bytes);
		}

		return new Checksum(bytes);
	}

	public void write(DataOutputStream output) throws IOException {
		output.writeInt(data.length);
		if (data.length > 0) {
			output.write(data);
		}
	}

	private String toString;

	public String toString() {
		if (toString == null) {
			toString = new String(Hex.encodeHex(data));
		}
		return toString;
	}

	public boolean equals(Object o) {
		return this == o || (o instanceof Checksum && equals((Checksum) o));
	}

	public boolean equals(Checksum other) {
		return this == other
				|| (other != null && Arrays.equals(data, other.data));
	}

	public int hashCode() {
		return data.hashCode();
	}

}
