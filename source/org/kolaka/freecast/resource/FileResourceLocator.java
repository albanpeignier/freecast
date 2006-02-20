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
package org.kolaka.freecast.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class FileResourceLocator implements ResourceLocator {

	private final FileResolver fileResolver;

	public FileResourceLocator() {
		fileResolver = new DefautFileResolver();
	}

	public FileResourceLocator(File baseDirectory) {
		fileResolver = new BasedFileResolver(baseDirectory);
	}

	public InputStream openResource(URI uri) throws ResourceLocator.Exception {
		Validate.notNull(uri, "No specified URI");
		if (uri.getScheme() != null) {
			ResourceLocator.MalformedURIException.checkScheme(uri, "file");
		}

		File file = fileResolver.resolve(uri);
		LogFactory.getLog(getClass()).debug("open " + file);
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new ResourceLocator.NoSuchResourceException(uri);
		}
	}

	public static interface FileResolver {

		File resolve(URI uri);

	}

	public static class DefautFileResolver implements FileResolver {

		/**
		 * @param uri
		 * @param path
		 * @return
		 */
		private boolean isAbsolutePath(URI uri) {
			String path = uri.getPath();
			return path.startsWith(SystemUtils.FILE_SEPARATOR)
					|| path.startsWith("/");
		}

		protected void listPossibleFiles(List candidates, String path) {

		}

		protected String getPath(URI uri) {
			String path = uri.getPath();

			return path;
		}

		public File resolve(URI uri) {
			String path = uri.getPath();

			if (isAbsolutePath(uri)) {
				return new File(path);
			}

			List candidates = new LinkedList();
			listPossibleFiles(candidates, path);

			for (Iterator iter = candidates.iterator(); iter.hasNext();) {
				File candidate = (File) iter.next();
				if (candidate.exists()) {
					return candidate;
				}
			}

			return new File(path);
		}

	}

	public static class BasedFileResolver extends DefautFileResolver {

		private final File baseDirectory;

		/**
		 * @param baseDirectory
		 */
		public BasedFileResolver(File baseDirectory) {
			this.baseDirectory = baseDirectory;
		}

		protected void listPossibleFiles(List candidates, String path) {
			candidates.add(0, new File(baseDirectory, path));
		}

	}

}
