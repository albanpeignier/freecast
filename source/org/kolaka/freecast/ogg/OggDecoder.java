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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.sound.AudioSystem;
import org.kolaka.freecast.sound.StereoPCMAudioInputStream;

public abstract class OggDecoder {

	private static final List INSTANCE_NAMES = Arrays.asList(new String[] {
			"javazoom" });

	private static OggDecoder instance;

	public static OggDecoder getInstance() {
		if (instance == null) {
			instance = createOggDecoder();
		}
		return instance;
	}

	private static OggDecoder createOggDecoder() {
		OggDecoder decoder = new Default();

		for (Iterator iter = INSTANCE_NAMES.iterator(); iter.hasNext();) {
			String name = (String) iter.next();

			try {
				decoder = createOggDecoder(name);
				break;
			} catch (NotAvailableException e) {
				LogFactory.getLog(OggDecoder.class).debug(
						"can't use " + name + " decoder", e);
			}
		}

		LogFactory.getLog(OggDecoder.class).debug("use " + decoder);
		return decoder;
	}

	public static OggDecoder createOggDecoder(String name)
			throws NotAvailableException {
		LogFactory.getLog(OggDecoder.class)
				.debug("create " + name + " decoder");
		try {
			return (OggDecoder) Class.forName(
					OggDecoder.class.getName() + "$"
							+ WordUtils.capitalize(name)).newInstance();
		} catch (Throwable t) {
			throw new NotAvailableException("can't create OggDecoder '" + name
					+ "'", t);
		}
	}

	public static class NotAvailableException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1748802281075825779L;

		/**
		 * @param message
		 * @param cause
		 */
		public NotAvailableException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	public AudioInputStream decode(InputStream inputStream)
			throws UnsupportedAudioFileException, IOException {
		AudioInputStream oggStream = createOggStream(inputStream);
		AudioFormat originalFormat = oggStream.getFormat();
		AudioFormat pcmFormat = createDefaultAudioFormat(originalFormat);
		return createDecodedStream(pcmFormat, oggStream);
	}

	protected abstract AudioInputStream createOggStream(InputStream inputStream)
			throws UnsupportedAudioFileException, IOException;

	private AudioInputStream createDecodedStream(AudioFormat pcmFormat,
			AudioInputStream oggStream) throws UnsupportedAudioFileException {
		try {
			return createDecodedStreamImpl(pcmFormat, oggStream);
		} catch (Throwable t) {
			UnsupportedAudioFileException exception = new UnsupportedAudioFileException(
					"can't decode " + oggStream.getFormat() + " to "
							+ pcmFormat);
			exception.initCause(t);
			throw exception;
		}
	}

	protected abstract AudioInputStream createDecodedStreamImpl(
			AudioFormat pcmFormat, AudioInputStream oggStream);

	public AudioInputStream decode(AudioFormat pcmFormat,
			InputStream inputStream) throws UnsupportedAudioFileException,
			IOException {
		AudioInputStream oggStream = createOggStream(inputStream);

		// TODO only decode oggStream.getFormat().getChannels() and create other
		// channels
		AudioFormat decodedFormat = pcmFormat;
		boolean monoToStereo = oggStream.getFormat().getChannels() == 1
				&& pcmFormat.getChannels() == 2;
		if (monoToStereo) {
			decodedFormat = new AudioFormat(oggStream.getFormat()
					.getSampleRate(), pcmFormat.getSampleSizeInBits(), 1, true,
					false);
		}

		AudioInputStream decodedStream = createDecodedStream(decodedFormat,
				oggStream);

		if (monoToStereo) {
			return new StereoPCMAudioInputStream(decodedStream);
		}

		return decodedStream;
	}

	/**
	 * @param originalFormat
	 * @return
	 */
	protected AudioFormat createDefaultAudioFormat(AudioFormat originalFormat) {
		AudioFormat pcmFormat = new AudioFormat(originalFormat.getSampleRate(),
				16, originalFormat.getChannels(), true, false);
		return pcmFormat;
	}

	public static class Default extends OggDecoder {

		/**
		 * @param inputStream
		 * @return
		 * @throws UnsupportedAudioFileException
		 * @throws IOException
		 */
		protected AudioInputStream createOggStream(InputStream inputStream)
				throws UnsupportedAudioFileException, IOException {
			return AudioSystem.getAudioInputStream(inputStream);
		}

		/**
		 * @param pcmFormat
		 * @param originalAudioInput
		 * @return
		 */
		protected AudioInputStream createDecodedStreamImpl(
				AudioFormat pcmFormat, AudioInputStream originalAudioInput) {
			return AudioSystem.getAudioInputStream(pcmFormat,
					originalAudioInput);
		}

	}

	public abstract static class Direct extends OggDecoder {

		private final AudioFileReader fileReader;

		private final FormatConversionProvider conversionProvider;

		/**
		 * @param fileReader
		 * @param conversionProvider
		 */
		public Direct(AudioFileReader fileReader,
				FormatConversionProvider conversionProvider) {
			this.fileReader = fileReader;
			this.conversionProvider = conversionProvider;
		}

		protected AudioInputStream createOggStream(InputStream inputStream)
				throws UnsupportedAudioFileException, IOException {
			return fileReader.getAudioInputStream(inputStream);
		}

		protected AudioInputStream createDecodedStreamImpl(
				AudioFormat pcmFormat, AudioInputStream originalAudioInput) {
			return conversionProvider.getAudioInputStream(pcmFormat,
					originalAudioInput);
		}

	}

	public static class Javazoom extends Direct {

		public Javazoom() {
			super(
					new javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader(),
					new javazoom.spi.vorbis.sampled.convert.VorbisFormatConversionProvider());
		}

	}

	/*
	public static class Tritonus extends Direct {

		public Tritonus() {
			super(
					new org.tritonus.sampled.file.vorbis.VorbisAudioFileReader(),
					new org.tritonus.sampled.convert.vorbis.VorbisFormatConversionProvider());
		}

	}
	*/

}
