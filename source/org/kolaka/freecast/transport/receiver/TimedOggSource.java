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

import org.apache.commons.lang.time.DateUtils;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.OggSource;

public class TimedOggSource implements OggSource {

	private static final int UNDEFINED = -1;

	private long initialAbsoluteGranulePosition = UNDEFINED;

	private final OggSource source;

	private final int frameRate;

	public TimedOggSource(OggSource source, int frameRate) {
		this.source = source;
		this.frameRate = frameRate;
	}

	public OggPage next() throws IOException {
		OggPage sourcePage = source.next();
		long timestamp = getTimestamp(sourcePage);
		return new DefaultTimedOggPage(timestamp, sourcePage);
	}

	private long getTimestamp(OggPage sourcePage) {
		long absoluteGranulePosition = sourcePage.getAbsoluteGranulePosition();

		if (initialAbsoluteGranulePosition == UNDEFINED) {
			initialAbsoluteGranulePosition = absoluteGranulePosition;
			return 0;
		}

		long relativePosition = absoluteGranulePosition
				- initialAbsoluteGranulePosition;
		return ((relativePosition * DateUtils.MILLIS_PER_SECOND) / frameRate);
	}

	public void close() throws IOException {
		source.close();
	}
	
	public String getDescription() {
		return source.getDescription();
	}

}
