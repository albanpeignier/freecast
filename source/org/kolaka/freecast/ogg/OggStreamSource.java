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

package org.kolaka.freecast.ogg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.input.SwappedDataInputStream;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.io.ReminderInputStream;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class OggStreamSource implements OggSource {

	private final String description;

	private final ReminderInputStream reminderInput;

	private final SwappedDataInputStream dataInput;

	public OggStreamSource(InputStream input) {
		this(input, String.valueOf(input));
	}

	public OggStreamSource(InputStream input, String description) {
		Validate.notEmpty(description, "No specified description");
		this.description = description;
		reminderInput = new ReminderInputStream(input);
		dataInput = new SwappedDataInputStream(new DataInputStream(
				reminderInput));
	}

	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("description", description);
		return builder.toString();
	}

	private final byte[] capturePatternBuffer = new byte[4];

	public OggPage next() throws IOException {
		dataInput.readFully(capturePatternBuffer, 0, 4);

		if (!Arrays.equals(OggPage.CAPTURE_PATTERN, capturePatternBuffer)) {
			throw new IOException("Missing capture pattern");
		}

		int streamStructureVersion = dataInput.readUnsignedByte();
		if (streamStructureVersion != 0 && streamStructureVersion != -1) {
			// [Bug 52] the last page can use a streamStructureVersion at -1
			throw new IOException("Bad stream structure version: "
					+ streamStructureVersion);
		}

		MutableOggPage page = new DefaultOggPage();

		int headerTypeFlag = dataInput.readUnsignedByte();
		page.setFirstPage((headerTypeFlag & 0x02) != 0);
		page.setLastPage((headerTypeFlag & 0x04) != 0);

		page.setAbsoluteGranulePosition(dataInput.readLong());
		page.setStreamSerialNumber(dataInput.readInt());

		dataInput.skip(8);

		int pageSegments = dataInput.readUnsignedByte();
		int pageSize = 0;

		for (int segmentIndex = 0; segmentIndex < pageSegments; segmentIndex++) {
			pageSize += dataInput.readUnsignedByte();
		}

		dataInput.skip(pageSize);

		page.setRawBytes(reminderInput.toByteArray());
		reminderInput.resetByteArray();

		// LogFactory.getLog(getClass()).trace("returns " + page);

		return page;
	}

	public void close() throws IOException {
		dataInput.close();
	}

}