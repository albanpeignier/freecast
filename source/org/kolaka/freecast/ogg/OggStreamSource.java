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

package org.kolaka.freecast.ogg;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.HexDump;
import org.apache.commons.io.input.SwappedDataInputStream;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
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
	
	public String getDescription() {
		return description;
	}

	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("description", description);
		return builder.toString();
	}

	private final byte[] capturePatternBuffer = new byte[4];
	
	private boolean beginOfStream = true;
	
	public OggPage next() throws IOException {
		dataInput.readFully(capturePatternBuffer, 0, 4);

		if (!Arrays.equals(OggPage.CAPTURE_PATTERN, capturePatternBuffer)) {
			LogFactory.getLog(getClass()).trace(createHexDump(capturePatternBuffer));
			throw createIOException("Missing capture pattern");
		}

		int streamStructureVersion = dataInput.readUnsignedByte();
		if (streamStructureVersion != 0 && streamStructureVersion != -1) {
			// [Bug 52] the last page can use a streamStructureVersion at -1
			throw createIOException("Bad stream structure version: "
					+ streamStructureVersion);
		}

    DefaultOggPage page = new DefaultOggPage();

		try {
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
			
      LogFactory.getLog(getClass()).trace("payload size: " + pageSize);
      
      byte payload[] = new byte[pageSize];
			dataInput.readFully(payload);
      page.setPayload(payload);

			page.setRawBytes(reminderInput.toByteArray());
			reminderInput.resetByteArray();
		} catch (EOFException e) {
			throw e;
		} catch (IOException e) {
		
			LogFactory.getLog(getClass()).error("io error while reading " + page);
			lastPages.log();
			byte[] readData = reminderInput.toByteArray();
			if (readData.length > 0) {
				LogFactory.getLog(getClass()).trace("last bytes read");
				LogFactory.getLog(getClass()).trace(createHexDump(readData));
			}
			throw e;
		}
		
		if (beginOfStream) {
			if (!page.isFirstPage()) {
				throw createIOException("stream must begin with a first page: " + page);
			}
			beginOfStream = false;
		}
		
		if (page.isLastPage()) {
			beginOfStream = true;
		}

		LogFactory.getLog(getClass()).trace("returns " + page);
		// LogFactory.getLog(getClass()).trace(createHexDump(page.getRawBytes()));
		
		lastPages.add(page);
		
		return page;
	}

	/**
	 * @param message
	 * @throws IOException
	 */
	private IOException createIOException(String message) {
		lastPages.log();
		return new IOException(message);
	}

	/**
	 * @param buffer TODO
	 * @return
	 * @throws IOException
	 */
	private String createHexDump(byte[] buffer) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		HexDump.dump(buffer, 0, output, 0);
		String hexdump = output.toString();
		output.close();
		return hexdump;
	}

	public void close() throws IOException {
		dataInput.close();
	}
	
	private final LastPages lastPages = new LastPages();
	
	class LastPages {
		
		private final int remaindedPages = 5;
		private final List pages = new LinkedList();

		public void add(OggPage page) {
			pages.add(page);
			while (pages.size() > remaindedPages) {
				pages.remove(0);
			}
		}

		public void log() {
			LogFactory.getLog(getClass()).trace("last read pages:");
			for (Iterator iter = pages.iterator(); iter.hasNext();) {
				OggPage page = (OggPage) iter.next();
				LogFactory.getLog(getClass()).trace(page);
				try {
					LogFactory.getLog(getClass()).trace(createHexDump(page.getRawBytes()));
				} catch (IOException e) {
					LogFactory.getLog(getClass()).error("can't dump " + page);
				}
			}
		}
		
		
		
	}

}