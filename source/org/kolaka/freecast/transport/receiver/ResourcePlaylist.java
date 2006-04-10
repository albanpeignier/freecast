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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.io.TextFile;
import org.kolaka.freecast.io.URLTextFile;
import org.kolaka.freecast.resource.ResourceLocator;
import org.kolaka.freecast.resource.URIParser;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ResourcePlaylist implements Playlist {

	private ResourceLocator locator;
	private URI definitionURI = URI.create("memory:/");

	private final List uris = new ArrayList();
	
	public void setDefinitionURI(URI definitionURI) {
		this.definitionURI = definitionURI;
	}
	
	public URI getDefinitionURI() {
		return definitionURI;
	}

	public ResourcePlaylist(ResourceLocator locator, List uris) {
		Validate.allElementsOfType(uris, URI.class);
		Validate.notEmpty(uris);

		this.locator = locator;
		this.uris.addAll(uris);
	}

	private static final URIParser PARSER = new URIParser();

	public static ResourcePlaylist getInstance(ResourceLocator locator,
			URI playlist) throws IOException {
		Validate.notNull(locator, "No specified ResourceLocator");
		List uris = new LinkedList();

		TextFile textFile = new TextFile();
		textFile.load(locator.openResource(playlist));

		for (Iterator iter = textFile.getLines().iterator(); iter.hasNext();) {
			URLTextFile.Line line = (URLTextFile.Line) iter.next();
			String content = line.getContent().trim();

			if (content.startsWith("#") || content.length() == 0) {
				continue;
			}

			try {
				URI lineURI = playlist.resolve(PARSER.parse(content));
				uris.add(lineURI);
			} catch (URISyntaxException e) {
				String message = "invalid uri at " + playlist + ":"
						+ line.getNumber();
				LogFactory.getLog(ResourcePlaylist.class).warn(message, e);
			}
		}

		if (uris.isEmpty()) {
			throw new IOException("invalid empty playlist at " + playlist);
		}

		ResourcePlaylist resourcePlaylist = new ResourcePlaylist(locator, uris);
		resourcePlaylist.setDefinitionURI(playlist);
		return resourcePlaylist;
	}

	public Playlist.Entry get(int index) throws IOException {
		final URI uri = (URI) uris.get(index);
		return new Playlist.Entry(uri.toString()) {
			public InputStream openStream() throws IOException {
				return new BufferedInputStream(locator.openResource(uri));
			}
		};

	}

	public int size() {
		return uris.size();
	}

}
