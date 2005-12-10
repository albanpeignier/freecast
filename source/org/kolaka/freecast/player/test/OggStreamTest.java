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

package org.kolaka.freecast.player.test;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class OggStreamTest extends TestCase {

	/**
	 * TODO use a resource provided by a common package
	 */
	private static final String OGG_FILE = "/org/kolaka/freecast/ogg/test/resources/sample.ogg";

	public void testStream() throws Exception {
		InputStream stream = getClass().getResourceAsStream(OGG_FILE);
		assertNotNull("no input stream", stream);

		testStream(stream);
	}

	private void testStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(format,
				AudioSystem.getAudioInputStream(stream));

		final byte buffer[] = new byte[1024 * 1024 * 2];

		while (true) {
			int read = audioStream.read(buffer);
			if (read == -1) {
				break;
			}
		}
	}

}