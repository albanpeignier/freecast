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

package org.kolaka.freecast.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class PublicAddressResolver {

	private static PublicAddressResolver instance;

	public static PublicAddressResolver getDefaultInstance() throws IOException {
		if (instance == null) {
			instance = new Cache(new ExternalReference(new URL(
					"http://www.freecast.org/reference")));
		}

		return instance;
	}

	public abstract InetAddress getPublicAddress() throws IOException;

	static class Cache extends PublicAddressResolver {

		private final PublicAddressResolver delegate;

		private InetAddress cache;

		public Cache(PublicAddressResolver delegate) {
			this.delegate = delegate;
		}

		public InetAddress getPublicAddress() throws IOException {
			if (cache == null) {
				cache = delegate.getPublicAddress();
			}
			return cache;
		}

	}

	static class ExternalReference extends PublicAddressResolver {

		private final URL externalURL;

		public ExternalReference(URL externalURL) {
			this.externalURL = externalURL;
		}

		public InetAddress getPublicAddress() throws IOException {
			String publicReference = IOUtils.toString(externalURL.openStream());
			return InetAddress.getByName(publicReference);
		}

	}

}
