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

import java.io.EOFException;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;

import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.ogg.test.OggTestResources;
import org.kolaka.freecast.resource.ResourceLocators;
import org.kolaka.freecast.transport.receiver.EncoderFormat;
import org.kolaka.freecast.transport.receiver.PlaylistEncoderOggSourceFactory;
import org.kolaka.freecast.transport.receiver.ResourcePlaylist;
import org.kolaka.freecast.transport.receiver.TimedOggPage;

public class PlaylistEncoderOggSourceFactoryTest extends TestCase {

	public void testPlay() throws Exception {
		// experienced many problems with javazoom and sample.ogg ?!
		URI resource = OggTestResources.getResource("sample.wav").toURI();
		
		ResourcePlaylist playlist = new ResourcePlaylist(ResourceLocators.getDefaultInstance(), Collections.singletonList(resource));
		EncoderFormat format = new EncoderFormat(1, 44100, 0);
		PlaylistEncoderOggSourceFactory factory = new PlaylistEncoderOggSourceFactory(playlist, format);
		
		OggSource source = factory.next();
		TimedOggPage last = null;
		try {
			
			while (true) {
				TimedOggPage page = (TimedOggPage) source.next();
				if (last != null && !last.isFirstPage()) {
					assertTrue(page + " timestamp must be greather than " + last, page.getTimestamp() > last.getTimestamp());
				}
				last = page;
			}
			
		} catch (EOFException e) {
			
		} 
		assertTrue(last.isLastPage());
	}
	
}
