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

package org.kolaka.freecast.net.test;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.kolaka.freecast.net.URLUtils;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class URLUtilsTest extends TestCase {

	public void testGetBaseURL() throws MalformedURLException {
		testGetBaseURL("http://somehost/directory/", "child");
		testGetBaseURL("http://somehost/", "child");
		testGetBaseURL("file:", "relativefile");
		testGetBaseURL("file://home/directory/", "file");

		testGetBaseURL("http://user:password@somehost/", "child");
	}

	protected void testGetBaseURL(String baseURL, String suffix)
			throws MalformedURLException {
		URL childURL = new URL(baseURL + suffix);
		assertEquals(new URL(baseURL), URLUtils.getBaseURL(childURL));
	}

}
