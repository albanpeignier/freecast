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

package org.kolaka.freecast.transport;

import java.net.InetSocketAddress;

import org.kolaka.freecast.peer.BasePeerConnectionFactory;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerConnectionFactoryException;
import org.kolaka.freecast.peer.PeerReceivingConnection;
import org.kolaka.freecast.peer.PeerReceivingConnectionFactory;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.event.VetoPeerConnectionOpeningException;
import org.kolaka.freecast.transport.cas.ConnectionAssistantClient;
import org.kolaka.freecast.transport.cas.ConnectionAssistantClientAware;

public class MinaPeerReceivingConnectionFactory extends
		BasePeerConnectionFactory implements PeerReceivingConnectionFactory, ConnectionAssistantClientAware {

	public PeerReceivingConnection create(Peer peer, PeerReference reference) throws PeerConnectionFactoryException {
		InetPeerReference inetReference = (InetPeerReference) reference;
		
		InetSocketAddress remoteAddress = inetReference.getSocketAddress();

		MinaPeerReceivingConnection connection = new MinaPeerReceivingConnection(
				remoteAddress);

		if (client != null) {
			connection.setConnectionAssistantClient(client);
		}
		
		connection.setNodeStatusProvider(getStatusProvider());

		try {
			fireVetoableConnectionOpening(connection);
		} catch (VetoPeerConnectionOpeningException e) {
			throw new PeerConnectionFactoryException("Veto on opening connection", e);
		}

		fireConnectionOpening(connection);
		return connection;
	}

	private ConnectionAssistantClient client;

	public void setConnectionAssistantClient(ConnectionAssistantClient client) {
		this.client = client;
	}

}
