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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.ogg.OggStreamSource;
import org.kolaka.freecast.transport.receiver.Playlist.Entry;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class PlaylistOggSourceFactory implements OggSourceFactory {

	private final Playlist playlist;

	public PlaylistOggSourceFactory(Playlist playlist) {
		this.playlist = playlist;
	}

	public PlaylistOggSourceFactory(URL url) throws IOException {
		this(new FilePlaylist(url));
	}

	private int nextPlayedIndex;

	public OggSource next() throws IOException {
		Entry entry = playlist.get(nextPlayedIndex);
		InputStream input = entry.openStream();
		nextPlayedIndex = (nextPlayedIndex + 1) % playlist.size();
		return new OggStreamSource(new BufferedInputStream(input), entry
				.getDescription());
	}

}
