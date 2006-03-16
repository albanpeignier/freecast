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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.CopyUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class HttpResourceLocator implements ResourceLocator {
	private HttpClient httpClient;

	private final DateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

	private Cache cache;

	public HttpResourceLocator() {
		// TODO try to use TransientFileCache
		this(new PersistentFileCache());
	}

	public HttpResourceLocator(Cache cache) {
		Validate.notNull(cache, "No specified Cache");
		this.cache = cache;
		this.httpClient = new HttpClient();
	}

	public void setHttpClient(HttpClient httpClient) {
		Validate.notNull(httpClient, "No specified HttpClient");
		this.httpClient = httpClient;
	}

	public void setCache(Cache cache) {
		Validate.notNull(cache, "No specified Cache");
		this.cache = cache;
	}

	private Set cachedURIs = new TreeSet();

	public InputStream openResource(URI uri) throws ResourceLocator.Exception {
		ResourceLocator.MalformedURIException.checkScheme(uri, "http");

		URL url;
		try {
			url = uri.toURL();
		} catch (MalformedURLException e) {
			throw new ResourceLocator.MalformedURIException(uri);
		}

		if (cachedURIs.contains(uri)) {
			try {
				return loadCachedResource(uri);
			} catch (Exception e) {
				LogFactory.getLog(getClass()).warn(
						"Can't use the cached resource for " + uri, e);
				cachedURIs.remove(uri);
			}
		}

		GetMethod httpRetrieve = new GetMethod(url.toExternalForm());

		try {
			Date localLastModified = cache.getLastModified(uri);
			LogFactory.getLog(getClass()).debug(
					"use the last modified date " + localLastModified + " for "
							+ uri);
			Header ifModifiedSince = new Header("If-Modified-Since", dateFormat
					.format(localLastModified));
			httpRetrieve.addRequestHeader(ifModifiedSince);
		} catch (IOException e) {
			LogFactory.getLog(getClass())
					.debug(
							"Can't retrieve the last modified date in cache for "
									+ uri);
		}

		int statusCode;

		try {
			statusCode = httpClient.executeMethod(httpRetrieve);
		} catch (IOException e) {
			throw new ResourceLocator.UnavailableResourceException(uri, e);
		}

		LogFactory.getLog(getClass()).debug(
				"retrieved " + uri + ", " + httpRetrieve.getStatusLine());

		InputStream resourceInput;

		if (statusCode == HttpStatus.SC_OK) {
			resourceInput = retrieveResource(uri, httpRetrieve);
		} else if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
			resourceInput = loadCachedResource(uri);
		} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
			throw new ResourceLocator.NoSuchResourceException(uri);
		} else {
			HttpException exception = new HttpException("Can't connect to "
					+ url + " (" + httpRetrieve.getStatusLine() + ")");
			throw new ResourceLocator.UnavailableResourceException(uri, exception);
		}

		return resourceInput;
	}

	private InputStream retrieveResource(URI uri, GetMethod httpRetrieve)
			throws ResourceLocator.Exception {
		try {
			InputStream httpRetrieveInput = httpRetrieve
					.getResponseBodyAsStream();
			Header lastModified = httpRetrieve
					.getResponseHeader("Last-Modified");
			if (lastModified != null) {
				Date serverLastModified = dateFormat.parse(lastModified
						.getValue());
				LogFactory.getLog(getClass()).debug(
						"cache the content of " + uri + " modified at "
								+ serverLastModified);
				cache.cache(uri, httpRetrieveInput, serverLastModified);
				httpRetrieve.releaseConnection();
				return loadCachedResource(uri);
			} else {
				LogFactory.getLog(getClass()).debug(
						"load directly the content of " + uri);
				return httpRetrieveInput;
			}
		} catch (IOException e) {
			throw new ResourceLocator.UnavailableResourceException(uri, e);
		} catch (ParseException e) {
			throw new ResourceLocator.UnavailableResourceException(uri, e);
		}
	}

	private InputStream loadCachedResource(URI uri) throws ResourceLocator.Exception {
		try {
			LogFactory.getLog(getClass()).debug(
					"use the cached content of " + uri);
			InputStream input = cache.getInputStream(uri);
			cachedURIs.add(uri); // reminds that URI has been cached
			return input;
		} catch (IOException e) {
			throw new ResourceLocator.UnavailableResourceException(uri, e);
		}
	}

	public static interface Cache {
		public Date getLastModified(URI uri) throws IOException;

		public void cache(URI uri, InputStream resource, Date lastModified)
				throws IOException;

		public InputStream getInputStream(URI uri) throws IOException;

	}

	public static abstract class FileCache implements Cache {

		protected abstract File getCacheFile(URI uri) throws IOException;

		protected String getFileName(URI uri) {
			return DigestUtils.shaHex(uri.toString());
		}

		public Date getLastModified(URI uri) throws IOException {
			File file = getCacheFile(uri);
			if (!file.exists()) {
				throw new FileNotFoundException();
			}
			return new Date(file.lastModified());
		}

		public void cache(URI uri, InputStream resource, Date lastModified)
				throws IOException {
			File file = getCacheFile(uri);
			OutputStream outputStream = new FileOutputStream(file);
			CopyUtils.copy(resource, outputStream);
			outputStream.close();
			file.setLastModified(lastModified.getTime());
			LogFactory.getLog(getClass()).debug(
					"close cached file " + file + " "
							+ new Date(file.lastModified()));
		}

		public InputStream getInputStream(URI uri) throws IOException {
			File cacheFile = getCacheFile(uri);
			LogFactory.getLog(getClass()).debug(
					"use cached file " + cacheFile + " "
							+ new Date(cacheFile.lastModified()));
			return new FileInputStream(cacheFile);
		}
	}

	public static class PersistentFileCache extends FileCache {
		private final File cacheDirectory;

		/**
		 * @todo remove this default constructor
		 */
		public PersistentFileCache() {
			this(getDefaultCacheDirectory());
		}

		static File getDefaultCacheDirectory() {
			File cacheDirectory = new File(SystemUtils.JAVA_IO_TMPDIR,
					"freecast-" + SystemUtils.USER_NAME);
			cacheDirectory.mkdirs();
			return cacheDirectory;
		}

		public PersistentFileCache(File cacheDirectory) {
			Validate.notNull(cacheDirectory, "No specified cache directory");
			Validate.isTrue(cacheDirectory.exists(), "The cache directory "
					+ cacheDirectory + " doesn't exist");

			this.cacheDirectory = cacheDirectory;
		}

		protected File getCacheFile(URI uri) {
			return new File(cacheDirectory, getFileName(uri));
		}

	}

	/**
	 * @todo fixed a lastmodified problem, lastmodified is modified after the
	 *       output.close() ..
	 */
	public static class TransientFileCache extends FileCache {

		protected File getCacheFile(URI uri) throws IOException {
			File tempFile = File.createTempFile(HttpResourceLocator.class
					.getName()
					+ "-", getFileName(uri));
			tempFile.deleteOnExit();
			return tempFile;
		}

	}

}
