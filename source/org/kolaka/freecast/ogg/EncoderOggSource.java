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

package org.kolaka.freecast.ogg;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.tritonus.lowlevel.ogg.Packet;
import org.tritonus.lowlevel.ogg.Page;
import org.tritonus.lowlevel.ogg.StreamState;
import org.tritonus.lowlevel.vorbis.Block;
import org.tritonus.lowlevel.vorbis.Comment;
import org.tritonus.lowlevel.vorbis.DspState;
import org.tritonus.lowlevel.vorbis.Info;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class EncoderOggSource implements OggSource {

	private final static int READ = 1024;

	private final AudioInputStream audioInput;

	private final float quality;

	private final Uninterleaver uninterleaver;

	public EncoderOggSource(AudioInputStream audioInput, float quality) {
		Validate.notNull(audioInput, "No specified AudioInputStream");
		this.audioInput = audioInput;

		Validate.isTrue(quality >= 0, "quality can be be negative");
		this.quality = quality;

		this.uninterleaver = new Uninterleaver(audioInput.getFormat());
	}

	private boolean initialized;

	private byte[] readBuffer;

	private StreamState streamState;

	private Page page;

	private Packet packet;

	private Info info;

	private Comment comment;

	private DspState dspState;

	private Block block;

	private void init() throws IOException {
		AudioFormat inputFormat = audioInput.getFormat();
		LogFactory.getLog(getClass()).debug("initialize ogg encoder to encode " + inputFormat);

		readBuffer = new byte[READ * audioInput.getFormat().getFrameSize()];

		streamState = new StreamState();
		page = new Page();
		packet = new Packet();

		info = new Info();
		comment = new Comment();
		dspState = new DspState();
		block = new Block();

		info.init();

		info.encodeInitVBR(inputFormat.getChannels(), (int) inputFormat
				.getSampleRate(), quality);

		comment.init();
		comment.addTag("ENCODER", "FreeCast Tritonus libvorbis");

		dspState.initAnalysis(info);
		block.init(dspState);

		streamState.init(new Random().nextInt());

		Packet header = new Packet();
		Packet header_comm = new Packet();
		Packet header_code = new Packet();

		dspState.headerOut(comment, header, header_comm, header_code);
		streamState.packetIn(header);
		streamState.packetIn(header_comm);
		streamState.packetIn(header_code);

		while (true) {
			int flushResult = streamState.flush(page);
			if (flushResult == 0) {
				break;
			}
			pages.add(createOggPage(page));
		}

		initialized = true;
	}

	private OggPage createOggPage(Page page) {
		DefaultOggPage oggPage = new DefaultOggPage();

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(page.getHeader());
			outputStream.write(page.getBody());

			oggPage.setRawBytes(outputStream.toByteArray());
			outputStream.close();
		} catch (IOException e) {
			throw new UnhandledException(
					"ByteArrayOutputStream shouldn't throw errors", e);
		}

		oggPage.setAbsoluteGranulePosition(page.getGranulePos());
		oggPage.setFirstPage(page.isBos());
		oggPage.setLastPage(page.isEos());
		oggPage.setStreamSerialNumber(page.getSerialNo());

		return oggPage;
	}

	private final List pages = new LinkedList();

	private boolean endOfStream;

	/**
	 * @inherited
	 */
	public OggPage next() throws IOException {
		if (!initialized) {
			init();
		}

		if (pages.isEmpty()) {
			if (endOfStream) {
				throw new EOFException();
			}

			fillCache();
			
			if (pages.isEmpty()) {
				throw new EOFException();
			}
		}

		return (OggPage) pages.remove(0);
	}

	static class Uninterleaver {

		private final int frameSize;

		private final int channels;

		private final int bytesPerSample;

		private final boolean bigEndian;

		private final float scale;

		private final float[][] buffer;

		Uninterleaver(AudioFormat inputFormat) {
			frameSize = inputFormat.getFrameSize();
			channels = inputFormat.getChannels();

			bigEndian = inputFormat.isBigEndian();
			bytesPerSample = frameSize / channels;
			int sampleSizeInBits = bytesPerSample * 8;
			scale = (float) Math.pow(2.0, sampleSizeInBits - 1);
			buffer = new float[channels][READ];
		}

		float[][] process(byte[] readBuffer, int frames) {
			for (int frame = 0; frame < frames; frame++) {
				for (int channel = 0; channel < channels; channel++) {
					int nSample = bytesToInt16(readBuffer, frame * frameSize
							+ channel * bytesPerSample);
					buffer[channel][frame] = nSample / scale;
				}
			}

			return buffer;
		}

		private int bytesToInt16(byte[] buffer, int byteOffset) {
			return bigEndian ? ((buffer[byteOffset] << 8) | (buffer[byteOffset + 1] & 0xFF))
					: ((buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF));
		}

	}
	
	private void fillCache() throws IOException {
		boolean cacheFilled = false;
		while (!cacheFilled) {
			int read = audioInput.read(readBuffer);

			if (read == 0 || read == -1) {
				dspState.write(null, 0);
				LogFactory.getLog(getClass()).debug("end of the read stream");
				endOfStream = true;
			} else {
				int frames = read / audioInput.getFormat().getFrameSize();
				float[][] buffer = uninterleaver.process(readBuffer, frames);
				dspState.write(buffer, frames);
			}

			while (dspState.blockOut(block) == 1) {
				block.analysis(null);
				block.addBlock();
				while (dspState.flushPacket(packet) != 0) {
					streamState.packetIn(packet);

					while (!page.isEos()) {
						int result = streamState.pageOut(page);

						if (result == 0) {
							break;
						}

						pages.add(createOggPage(page));
						cacheFilled = true;
					}
				}
			}
		}
	}

	/**
	 * @inherited
	 */
	public void close() throws IOException {
		if (streamState != null) {
			streamState.clear();
			// streamState.free();
		}
		if (block != null) {
			block.clear();
			// block.free();
		}
		if (dspState != null) {
			dspState.clear();
			// dspState.free();
		}
		if (comment != null) {
			comment.clear();
			// comment.free();
		}
		if (info != null) {
			info.clear();
			// info.free();
		}
		/*
		if (page != null) {
			// page.free();
		}
		if (packet != null) {
			packet.clear();
			// packet.free();
		}
		*/
	}

}
