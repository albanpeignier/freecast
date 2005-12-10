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
package org.kolaka.freecast.peer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.apache.commons.lang.Validate;
import org.kolaka.freecast.net.PublicAddressResolver;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class AutomaticPeerReferenceFactory implements PeerReferenceFactory {

	private int defaultPort = UNDEFINED;

	private PublicAddressResolver publicAddressResolver;

	private static final int UNDEFINED = -1;

	public void setDefaultPort(int defaultPort) {
		Validate.isTrue(InetPeerReference.validatePort(defaultPort),
				"Invalid port: " + defaultPort);
		this.defaultPort = defaultPort;
	}

	public void setPublicAddressResolver(
			PublicAddressResolver publicAddressResolver) {
		Validate.notNull(publicAddressResolver,
				"No specified PublicAddressResolver");
		this.publicAddressResolver = publicAddressResolver;
	}

	public PeerReference create() throws PeerReferenceFactoryException {
		if (defaultPort == UNDEFINED) {
			throw new IllegalStateException("No defined default port");
		}

		Set addresses;
		try {
			addresses = getLocalInetAddress();
		} catch (IOException e) {
			throw new PeerReferenceFactoryException(
					"Can't determinate the local inet addresses", e);
		}

		try {
			addresses.add(getPublicAddress());
		} catch (IOException e) {
			throw new PeerReferenceFactoryException(
					"Can't determinate the public inet addresses", e);
		}

		Set references = new HashSet();
		for (Iterator iter = addresses.iterator(); iter.hasNext();) {
			InetAddress address = (InetAddress) iter.next();
			references.add(InetPeerReference.getInstance(address
					.getHostAddress(), defaultPort, false));
		}
		return new MultiplePeerReference(references);
	}

	private InetAddress getPublicAddress() throws IOException {
		if (publicAddressResolver == null) {
			publicAddressResolver = PublicAddressResolver.getDefaultInstance();
		}
		return publicAddressResolver.getPublicAddress();
	}

	private Set getLocalInetAddress() throws IOException {
		Set inetAddresses = new HashSet();

		Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = (NetworkInterface) networkInterfaces
					.nextElement();
			inetAddresses.addAll(EnumerationUtils.toList(networkInterface
					.getInetAddresses()));
		}

		CollectionUtils.filter(inetAddresses, new InstanceofPredicate(
				Inet4Address.class));

		if (inetAddresses.size() > 1) {
			Predicate loopback = new Predicate() {

				public boolean evaluate(Object object) {
					return ((InetAddress) object).isLoopbackAddress();
				}
			};
			CollectionUtils.filter(inetAddresses, new NotPredicate(loopback));
		}

		return inetAddresses;
	}

}
