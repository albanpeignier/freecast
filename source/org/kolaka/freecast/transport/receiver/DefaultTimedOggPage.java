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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.ogg.OggPage;

public class DefaultTimedOggPage implements TimedOggPage {

	private final long timestamp;

	private final OggPage page;

	public DefaultTimedOggPage(long timestamp, OggPage page) {
		this.timestamp = timestamp;
		this.page = page;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public byte[] getRawBytes() {
		return page.getRawBytes();
	}

	public boolean isFirstPage() {
		return page.isFirstPage();
	}

	public boolean isLastPage() {
		return page.isLastPage();
	}

	public long getAbsoluteGranulePosition() {
		return page.getAbsoluteGranulePosition();
	}

	public int getStreamSerialNumber() {
		return page.getStreamSerialNumber();
	}

	public String getStreamSerialNumberString() {
		return page.getStreamSerialNumberString();
	}

	public int getLength() {
		return page.getLength();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
