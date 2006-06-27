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

package org.kolaka.freecast.ogg.test;

import java.util.Random;

import org.kolaka.freecast.ogg.DefaultOggPage;
import org.kolaka.freecast.ogg.OggPage;

public class OggPageGenerator {

	private int absoluteGranulePosition;

	private boolean firstPage = true;

	private int streamSerialNumber = new Random().nextInt();

	public OggPage generate() {
    DefaultOggPage page = new DefaultOggPage();
		page.setAbsoluteGranulePosition(absoluteGranulePosition);
		page.setFirstPage(firstPage);
		page.setLastPage(false);
		page.setStreamSerialNumber(streamSerialNumber);
		int length = 100;
		page.setRawBytes(new byte[length]);

		absoluteGranulePosition += 44100 * 10;
		if (firstPage) {
			firstPage = false;
		}

		return page;
	}

}
