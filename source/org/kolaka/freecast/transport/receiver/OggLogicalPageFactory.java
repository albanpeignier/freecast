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

package org.kolaka.freecast.transport.receiver;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.Validate;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.packet.Checksum;
import org.kolaka.freecast.packet.DefaultPacket;
import org.kolaka.freecast.packet.DefaultPacketData;
import org.kolaka.freecast.packet.LogicalPage;
import org.kolaka.freecast.packet.LogicalPageBuilder;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.packet.PacketData;
import org.kolaka.freecast.packet.SequenceGenerator;
import org.kolaka.freecast.packet.signer.DummyPacketChecksummer;
import org.kolaka.freecast.packet.signer.PacketChecksummer;
import org.kolaka.freecast.packet.signer.PacketChecksummerException;
import org.kolaka.freecast.timer.DefaultTimeBase;
import org.kolaka.freecast.timer.TimeBase;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class OggLogicalPageFactory {

	private static TimeBase timeBase = DefaultTimeBase.DEFAULT;

	private OggSource source;

	private SequenceGenerator pageIdentifierGenerator = new SequenceGenerator();

	private SequenceGenerator packetIdentifierGenerator = new SequenceGenerator();

	private PacketChecksummer checksummer = new DummyPacketChecksummer();

	public static void setTimeBase(TimeBase base) {
		Validate.notNull(base, "No specified TimeBase");
		timeBase = base;
	}

	public void setPacketChecksummer(PacketChecksummer checksummer) {
		Validate.notNull(checksummer, "No specified PacketChecksummer");
		this.checksummer = checksummer;
	}

	public void setSource(OggSource source) {
		Validate.notNull(source, "No specified OggSource");
		this.source = source;
	}

	private OggPage nextOggPage;

	public LogicalPage next() throws IOException {
		List nextOggPages = nextOggPages();
		long sequenceNumber = pageIdentifierGenerator.next();
		OggPage firstOggPage = (OggPage) nextOggPages.get(0);

		List packetDatas = createPacketDatas(nextOggPages);

		long timestamp = getTimestamp(firstOggPage);
		boolean isFirstPage = firstOggPage.isFirstPage();

		LogicalPageBuilder builder = new LogicalPageBuilder(sequenceNumber,
				timestamp, packetDatas.size(), isFirstPage);
		for (ListIterator iter = packetDatas.listIterator(); iter.hasNext();) {
			int index = iter.nextIndex();
			PacketData packetData = (PacketData) iter.next();
			Checksum checksum;
			try {
				checksum = checksummer.checksum(packetData);
			} catch (PacketChecksummerException e) {
				IOException exception = new IOException(
						"Can't sign the PacketData " + packetData);
				exception.initCause(e);
				throw exception;
			}
			Packet packet = new DefaultPacket(packetIdentifierGenerator.next(),
					timestamp, packetData, checksum, builder
							.createElementDescriptor(index));
			builder.add(packet);
		}
		return builder.create();
	}

	private long beginOfStreamTimestamp, lastTimestamp = -1;

	/**
	 * @param firstOggPage
	 * @return
	 */
	private long getTimestamp(OggPage firstOggPage) {
		if (!(firstOggPage instanceof TimedOggPage)) {
			return timeBase.currentTimeMillis();
		}

		if (firstOggPage.isFirstPage()) {
			if (lastTimestamp == -1) {
				// if needed, initialize lastTimeStamp with "current" time
				lastTimestamp = timeBase.currentTimeMillis();
			}
			/*
			 * when a stream begins the ogg page timestamp is reseted so we use
			 * the timestamp of the previous processed OggPage
			 */
			beginOfStreamTimestamp = lastTimestamp;
		}

		long timestamp = beginOfStreamTimestamp
				+ ((TimedOggPage) firstOggPage).getTimestamp();
		lastTimestamp = timestamp;
		return timestamp;
	}

	private int maximumPacketSize = Packet.DEFAULT_SIZE;

	private List createPacketDatas(List nextOggPages) {
		List packetDatas = new LinkedList();

		for (Iterator iter = nextOggPages.iterator(); iter.hasNext();) {
			OggPage oggPage = (OggPage) iter.next();
			packetDatas.addAll(createPacketDatas(oggPage));
		}

		return packetDatas;
	}

	private List createPacketDatas(OggPage oggPage) {
		List packetDatas = new LinkedList();

		byte[] oggData = oggPage.getRawBytes();
		int packetSize = maximumPacketSize;

		int packetCount = (int) Math.ceil((double) oggData.length / packetSize);

		for (int i = 0; i < packetCount; i++) {
			int offset = i * packetSize;
			int size = Math.min(oggData.length - offset, packetSize);

			byte[] bytes = new byte[size];
			System.arraycopy(oggData, offset, bytes, 0, size);

			packetDatas.add(new DefaultPacketData(bytes));
		}

		return packetDatas;
	}

	/**
	 * Extracts from the <code>OggSource</code> the next <code>OggPage</code>
	 * instances to be included into a <code>LogicalPage</code>.
	 */
	private List nextOggPages() throws IOException {
		if (source == null) {
			throw new IllegalStateException("No defined OggSource");
		}

		OggPage page;

		// use the buffered Ogg page if present
		if (nextOggPage != null) {
			page = nextOggPage;
			nextOggPage = null;
		} else {
			page = source.next();
		}

		// "normal" OggPage are read one by one
		if (!page.isFirstPage()) {
			return Collections.singletonList(page);
		}

		// header OggPages are grouped
		List headerPages = new LinkedList();
		while (page.getAbsoluteGranulePosition() == 0) {
			headerPages.add(page);
			page = source.next();
		}
		nextOggPage = page;
		return headerPages;
	}

}
