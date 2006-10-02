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

package org.kolaka.freecast.transport.receiver;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.EncoderOggSource;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.sound.AudioSystem;
import org.kolaka.freecast.transport.receiver.Playlist.Entry;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class PlaylistEncoderOggSourceFactory implements OggSourceFactory {

	private final EncoderFormat format;

	private final Playlist playlist;

	private final AudioFormat readFormat;

	public PlaylistEncoderOggSourceFactory(Playlist playlist,
			EncoderFormat format) {
		Validate.notNull(playlist);
		Validate.notNull(format);

		this.playlist = playlist;
		this.format = format;
		this.readFormat = new AudioFormat(format.getSampleRate(), 16, format
				.getChannels(), true, false);
	}

	public PlaylistEncoderOggSourceFactory(URL url, EncoderFormat format)
			throws IOException {
		this(new FilePlaylist(url), format);
	}

	private int nextPlayedIndex;

	public OggSource next() throws IOException {
		Entry entry = playlist.get(nextPlayedIndex);

		AudioInputStream audioInput;
		try {
			AudioInputStream originalAudioInput = AudioSystem
					.getAudioInputStream(entry.openStream());
			audioInput = AudioSystem.getAudioInputStream(readFormat,
					originalAudioInput);
		} catch (UnsupportedAudioFileException e) {
			IOException exception = new IOException("Can't read "
					+ entry.getDescription());
			exception.initCause(e);
			throw exception;
		}

		LogFactory.getLog(getClass())
				.info(
						"play " + entry.getDescription() + " ["
								+ nextPlayedIndex + "]");

		nextPlayedIndex = (nextPlayedIndex + 1) % playlist.size();
		return new TimedOggSource(new EncoderOggSource(audioInput, entry.getDescription(), format
				.getQuality()), format.getSampleRate());
	}
  
  public EncoderFormat getFormat() {
    return format;
  }
  
  public URI getPlaylistURI() {
    return playlist.getDefinitionURI();
  }

}
