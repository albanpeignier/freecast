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

package org.kolaka.freecast.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URLTextFile extends TextFile {

	private final URL url;

	public URLTextFile(File file) throws MalformedURLException {
		this(file.toURL());
	}

	public URLTextFile(URL url) {
		this.url = url;
	}

	public URLTextFile(URI uri) throws MalformedURLException {
		this(uri.toURL());
	}

	public void load() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(url
				.openStream()));
		load(reader);
	}

	protected void checkLoading() throws IOException {
		if (!isLoaded()) {
			load();
		}
	}

}