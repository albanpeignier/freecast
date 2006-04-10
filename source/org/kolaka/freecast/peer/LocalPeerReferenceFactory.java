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

package org.kolaka.freecast.peer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.apache.commons.lang.Validate;

public class LocalPeerReferenceFactory implements PeerReferenceFactory {
	
	private final int port;

	public LocalPeerReferenceFactory(int port) {
		Validate.isTrue(InetPeerReference.validatePort(port));
		this.port = port;
	}

	public PeerReference create() throws PeerReferenceFactoryException {
		try {
			return InetPeerReferences.create(getLocalInetAddress(), port);
		} catch (SocketException e) {
			throw new PeerReferenceFactoryException("can't determinate local addresses", e);
		}
	}
	
	private Set getLocalInetAddress() throws SocketException {
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
