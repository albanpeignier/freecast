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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerReferenceLoader {

	private InetSocketAddress listenAddress;

	public void setListenAddress(InetSocketAddress listenAddress) {
		this.listenAddress = listenAddress;
	}

	protected InetSocketAddress getListenAddress() {
		if (listenAddress == null) {
			throw new IllegalStateException("No defined listen address");
		}
		return listenAddress;
	}

	public PeerReference load(Configuration configuration)
			throws ConfigurationException {
		if (configuration.isEmpty()) {
			return InetPeerReference.getInstance(listenAddress, false);
		}

		return createReference(configuration);
	}

	private PeerReference createReference(final Configuration configuration)
			throws ConfigurationException {
		String referenceClass = configuration.getString("class", "inet");
		
		PeerReferenceFactory factory;
		
		if (referenceClass.equals("inet")) {
			factory = new PeerReferenceFactory() {
				public PeerReference create() throws PeerReferenceFactoryException {
					return InetPeerReference.getInstance(configuration
							.getString("host"), configuration.getInt("port",
							listenAddress.getPort()), true);
				};
			};
		} else if (referenceClass.equals("multiple")) {
			factory = new PeerReferenceFactory() {
				public PeerReference create() throws PeerReferenceFactoryException {
			Set references = new HashSet();
			// TODO horrible limitation of Commons Configuration ..
			List hostNames = configuration.getList("reference.host");
			for (Iterator iterator = hostNames.iterator(); iterator.hasNext();) {
				String hostName = (String) iterator.next();
				references.add(InetPeerReference.getInstance(hostName,
						listenAddress.getPort(), true));
			}
			return new MultiplePeerReference(references);
				}};
		} else if (referenceClass.equals("auto")) {
			factory = new AutomaticPeerReferenceFactory(listenAddress.getPort());
		} else if (referenceClass.equals("stun")) {
			InetSocketAddress stunServer = new InetSocketAddress( 
			configuration.getString("host"), configuration.getInt("port",3478));
			factory = new StunPeerReferenceFactory(listenAddress.getPort(), stunServer);
		} else {
			throw new ConfigurationException("Unknow reference class: "
					+ referenceClass);
		}
		
		try {
			return factory.create();
		} catch (PeerReferenceFactoryException e) {
			throw new ConfigurationException(
					"Can't create the automatic reference", e);
		}
	}

}
