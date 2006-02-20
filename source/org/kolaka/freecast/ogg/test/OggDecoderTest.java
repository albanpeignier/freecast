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

package org.kolaka.freecast.ogg.test;

import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.FormatConversionProvider;

import junit.framework.TestCase;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.OggDecoder;
import org.kolaka.freecast.resource.ResourceLocator;
import org.kolaka.freecast.resource.ResourceLocators;

import org.apache.commons.discovery.tools.Service;

public class OggDecoderTest extends TestCase {

	public void testConversionProviderAvailable() {
	    testAvailable(FormatConversionProvider.class);
	}

	public void testFileReaderAvailable() {
	    testAvailable(AudioFileReader.class);
	}

    private void testAvailable(Class spi) {
		List providers = IteratorUtils.toList(IteratorUtils.asIterator(Service.providers(spi)));
		assertFalse(providers.isEmpty());
		LogFactory.getLog(getClass()).debug("implementation of " + spi + ": " + providers);
	}

	public void testJavaZoom() throws Exception {
		if (isEnabled("javazoom")) {
			testProvider(OggDecoder.createOggDecoder("javazoom"));
		}
	}

	/*
	public void testTritonus() throws Exception {
		if (isEnabled("tritonus")) {
			testProvider(OggDecoder.createOggDecoder("tritonus"));
		}
	}
	*/

	public void testDefault() throws Exception {
		testProvider(OggDecoder.getInstance());
	}

	private boolean isEnabled(String provider) {
		String propertyName = getClass().getName() + "." + provider;
		String property = System.getProperty(propertyName);
		return property == null || !property.equals("false");
	}

	private void testProvider(OggDecoder decoder) throws Exception {
		InputStream inputResources = getResourceAsStream();
		AudioInputStream pcmInputStream = decoder.decode(inputResources);

		long readLength = 0;
		int read = 0;
		byte[] buffer = new byte[1024 * pcmInputStream.getFormat()
				.getFrameSize()];
		Checksum checksum = new CRC32();
		while (read > -1) {
			read = pcmInputStream.read(buffer);
			if (read > -1) {
				readLength += read;
				checksum.update(buffer, 0, read);
			}
		}
		LogFactory.getLog(getClass()).debug(
				"read length: " + readLength + " checksum: "
						+ Long.toHexString(checksum.getValue()));

		pcmInputStream.close();
	}

	/**
	 * @return
	 * @throws ResourceLocator.Exception
	 * @throws Exception
	 */
	private InputStream getResourceAsStream() throws ResourceLocator.Exception {
		InputStream inputResources;
		String property = System
				.getProperty(getClass().getName() + ".resource");

		if (property == null || property.equals("default")) {
			LogFactory.getLog(getClass()).debug("load default resource");
			inputResources = OggTestResources.getResourceAsStream("sample.ogg");
		} else {
			LogFactory.getLog(getClass()).debug("load resource: " + property);
			inputResources = ResourceLocators.getDefaultInstance()
					.openResource(URI.create(property));
		}
		return inputResources;
	}

}
