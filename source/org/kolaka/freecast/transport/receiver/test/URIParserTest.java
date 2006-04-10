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

package org.kolaka.freecast.transport.receiver.test;

import java.net.URI;

import junit.framework.TestCase;

import org.kolaka.freecast.resource.URIParser;

public class URIParserTest extends TestCase {

	public void testPathURIParsing() throws Exception {
		testPathURIParsing("/directory/file with spaces.ogg",
				"/directory/file with spaces.ogg");
		testPathURIParsing("/directory/filewithquote'.ogg",
				"/directory/filewithquote'.ogg");
		testPathURIParsing("file:\\c\\directory\\file.ogg",
				"/c/directory/file.ogg");
	}

	private void testPathURIParsing(String string, String expectedPath)
			throws Exception {
		URI uri = new URIParser().parse(string);
		assertEquals("wrong path into " + uri, expectedPath, uri.getPath());
	}

	public void testURIParsing() throws Exception {
		testURIParsing("http://host/path", new URI("http", "host", "/path",
				null));
	}

	private void testURIParsing(String string, URI expectedURI)
			throws Exception {
		URI uri = new URIParser().parse(string);
		assertEquals(expectedURI, uri);
	}

}
