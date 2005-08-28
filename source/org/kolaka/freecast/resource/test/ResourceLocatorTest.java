/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2005 Alban Peignier
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
package org.kolaka.freecast.resource.test;

import junit.framework.TestCase;
import org.kolaka.freecast.resource.*;

import java.io.File;
import java.net.URI;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ResourceLocatorTest extends TestCase {

	private static final String RESOURCE_PREFIX = "org/kolaka/freecast/resource/test/resources/";
	private static final String RESOURCE_RELATIVENAME = "ClassLoaderResourceLocator.properties";
	private static final String RESOURCE_NAME = RESOURCE_PREFIX + RESOURCE_RELATIVENAME;
	private static final String HTTP_URL = "http://download.freecast.org/jws/stable/config.xml";

	private URI httpURI, fileURI, resourceURI, prefixURI, relativeURI;
	private File file;

	protected void setUp() throws Exception {
		super.setUp();

		httpURI = new URI(HTTP_URL);

		file = File.createTempFile(getClass().getName(), ".tmp");
		fileURI = file.toURI();

		resourceURI = new URI(RESOURCE_NAME);

		prefixURI = new URI(RESOURCE_PREFIX);
		relativeURI = new URI(RESOURCE_RELATIVENAME);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		file.delete();
	}

	public void testHttpResource() throws Exception {
		HttpResourceLocator resourceLocator = new HttpResourceLocator();
		resourceLocator.setCache(new HttpResourceLocator.PersistentFileCache());

		for (int i = 0; i < 3; i++) {
			resourceLocator.openResource(httpURI);
		}
	}

	public void testFileResource() throws Exception {
		FileResourceLocator resourceLocator = new FileResourceLocator();
		resourceLocator.openResource(fileURI);
	}

	public void testClassLoaderResource() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		assertNotNull(classLoader.getResource(RESOURCE_NAME));

		ResourceLocator locator = new ClassLoaderResourceLocator(classLoader);

		locator.openResource(resourceURI);
	}

	public void testPrefixedResource() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();

		ResourceLocator locator = new PrefixResourceLocator(prefixURI, new ClassLoaderResourceLocator(classLoader));
        locator.openResource(relativeURI);
	}

	public void testURLResource() throws Exception {
		ResourceLocator locator = new URLResourceLocator();
		locator.openResource(httpURI);
		locator.openResource(fileURI);
	}

	public void testDefaultResourceLocator() throws ResourceLocator.Exception {
		ResourceLocator locator = ResourceLocators.getDefaultInstance();
		locator.openResource(httpURI);
		locator.openResource(fileURI);
		locator.openResource(resourceURI);
	}

}
