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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.kolaka.freecast.ogg.EncoderOggSource;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.test.BaseTestCase;

public class EncoderOggSourceNativeTest extends BaseTestCase {

	public void testEncoding() throws Exception {
		InputStream inputResources = OggTestResources
				.getResourceAsStream("sample.ogg");

		AudioFormat pcmFormat = new AudioFormat(44100, 16, 1, true, false);
		AudioInputStream audioInput = AudioSystem.getAudioInputStream(
				pcmFormat, AudioSystem.getAudioInputStream(inputResources));

		OggSource oggSource = new EncoderOggSource(audioInput, "test", 0);

		ByteArrayOutputStream testOutput = new ByteArrayOutputStream();

		try {
			while (true) {
				OggPage page = oggSource.next();
				testOutput.write(page.getRawBytes());
			}
		} catch (EOFException e) {

		} finally {
			testOutput.close();
			oggSource.close();
			audioInput.close();
		}
	}

}