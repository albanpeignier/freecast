/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004 Alban Peignier
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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class InetPeerReference extends PeerReference {
	static final long serialVersionUID = 6228018933702558617L;

	public abstract InetSocketAddress getSocketAddress();

	int getPort() {
		return getSocketAddress().getPort();
	}

	public static boolean validatePort(int port) {
		return port > 0 && port < Math.pow(2, 16);
	}

	static class Address extends InetPeerReference {
		static final long serialVersionUID = -764731688638760248L;

		/**
		 * <strong>Note: </strong> final fields are supported by the Hessian
		 * serialization
		 */
		private InetSocketAddress socketAddress;

		public InetSocketAddress getSocketAddress() {
			return socketAddress;
		}

		private Address() {

		}

		Address(InetSocketAddress address) {
			this.socketAddress = address;
		}

		public boolean equals(PeerReference other) {
			return other instanceof InetPeerReference
					&& equals((InetPeerReference) other);
		}

		public boolean equals(InetPeerReference other) {
			return socketAddress.equals(other.getSocketAddress());
		}

		public int hashCode() {
			return socketAddress.hashCode();
		}

	}

	static class Host extends InetPeerReference {
		static final long serialVersionUID = 7973394067540991731L;

		/**
		 * <strong>Note: </strong> final fields are supported by the Hessian
		 * serialization
		 */
		private String host;

		private int port;

		public InetSocketAddress getSocketAddress() {
			return new InetSocketAddress(host, port);
		}

		public boolean equals(PeerReference other) {
			return other instanceof InetPeerReference
					&& equals((InetPeerReference) other);
		}

		public boolean equals(InetPeerReference other) {
			if (other instanceof InetPeerReference.Host) {
				return equals((InetPeerReference.Host) other);
			}

			InetSocketAddress otherAddress = other.getSocketAddress();
			return host.equals(otherAddress.getHostName())
					&& port == otherAddress.getPort();
		}

		public boolean equals(InetPeerReference.Host other) {
			return other != null && host.equals(other.host)
					&& port == other.port;
		}

		public int hashCode() {
			HashCodeBuilder builder = new HashCodeBuilder();
			builder.append(host);
			builder.append(port);
			return builder.toHashCode();
		}

		private Host() {

		}

		public Host(final String host, final int port) {
			Validate.notNull(host, "No specified host");

			this.host = host;
			this.port = port;
		}

		int getPort() {
			return port;
		}

	}

	public static InetPeerReference getInstance(InetSocketAddress socketAddress) {
		return getInstance(socketAddress, false);
	}

	public static InetPeerReference getInstance(
			InetSocketAddress socketAddress, boolean conserveHostName) {
		if (conserveHostName) {
			return new Host(socketAddress.getHostName(), socketAddress
					.getPort());
		}

		return new Address(socketAddress);
	}

	public static InetPeerReference getInstance(String host, int port,
			boolean conserveHostName) {
		if (conserveHostName) {
			return new Host(host, port);
		}

		return new Address(new InetSocketAddress(host, port));
	}

	public InetPeerReference specifyAddress(InetAddress address) {
		return getInstance(new InetSocketAddress(address, getPort()), false);
	}

	protected String getReferenceString() {
		return getSocketAddress().toString();
	}

}