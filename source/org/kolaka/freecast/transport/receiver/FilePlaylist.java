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
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kolaka.freecast.io.URLTextFile;
import org.kolaka.freecast.net.URLUtils;
import org.kolaka.freecast.resource.URIs;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class FilePlaylist implements Playlist {

	private List urls = new LinkedList();
	
	private final URI definitionURI;
	
	public URI getDefinitionURI() {
		return definitionURI;
	}

	public FilePlaylist(URL playlist) throws IOException {
		definitionURI = URIs.toURI(playlist);
		
		URLTextFile textFile = new URLTextFile(playlist);
		textFile.load();

		baseURL = URLUtils.getBaseURL(playlist);

		for (Iterator iter = textFile.getLines().iterator(); iter.hasNext();) {
			String content = ((URLTextFile.Line) iter.next()).getContent()
					.trim();
			URL lineURL;

			if (content.startsWith("#") || content.length() == 0) {
				continue;
			}

			lineURL = new URL(baseURL, content);

			urls.add(lineURL);
		}
	}

	private URL baseURL;

	public URL getBaseURL() {
		return baseURL;
	}

	public Playlist.Entry get(int index) throws IOException {
		final URL url = (URL) urls.get(index);
		return new Playlist.Entry(url.toExternalForm()) {
			public InputStream openStream() throws IOException {
				return url.openStream();
			}
		};

	}

	public int size() {
		return urls.size();
	}

}
