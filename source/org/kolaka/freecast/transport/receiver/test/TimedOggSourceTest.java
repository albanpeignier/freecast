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

package org.kolaka.freecast.transport.receiver.test;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.time.DateUtils;
import org.easymock.MockControl;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.ogg.test.OggPageGenerator;
import org.kolaka.freecast.transport.receiver.TimedOggPage;
import org.kolaka.freecast.transport.receiver.TimedOggSource;

public class TimedOggSourceTest extends TestCase {

	private MockControl sourceControl;

	private OggSource source;

	private TimedOggSource timedSource;

	private int frameRate = 44100;
	private int channels = 2;
	
	protected void setUp() throws Exception {
		sourceControl = MockControl.createControl(OggSource.class);
		source = (OggSource) sourceControl.getMock();
		timedSource = new TimedOggSource(source, frameRate);
	}

	/**
	 * Test method for
	 * 'org.kolaka.freecast.transport.receiver.TimedOggSource.next()'
	 * 
	 * @throws IOException
	 */
	public void testNext() throws IOException {
		source.next();
		OggPageGenerator oggPageGenerator = new OggPageGenerator();

		OggPage firstSourcePage = oggPageGenerator.generate();
		sourceControl.setReturnValue(firstSourcePage);

		OggPage secondSourcePage = oggPageGenerator.generate();
		sourceControl.setReturnValue(secondSourcePage);

		sourceControl.replay();

		TimedOggPage firstResultPage = (TimedOggPage) timedSource.next();
		assertEquals(firstSourcePage, firstResultPage);
		assertEquals(0, firstResultPage.getTimestamp());

		TimedOggPage secondResultPage = (TimedOggPage) timedSource.next();
		assertEquals(secondSourcePage, secondResultPage);

		long expectedTimestamp = (secondResultPage.getAbsoluteGranulePosition() - firstResultPage
				.getAbsoluteGranulePosition()) / frameRate * DateUtils.MILLIS_PER_SECOND;
		assertEquals(expectedTimestamp, secondResultPage.getTimestamp());

		sourceControl.verify();
	}

	/**
	 * Test method for
	 * 'org.kolaka.freecast.transport.receiver.TimedOggSource.close()'
	 * 
	 * @throws IOException
	 */
	public void testClose() throws IOException {
		source.close();

		sourceControl.replay();

		timedSource.close();

		sourceControl.verify();

	}

}
