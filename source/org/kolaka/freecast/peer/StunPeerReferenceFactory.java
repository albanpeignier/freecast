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

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.lang.Validate;
import org.kolaka.freecast.net.StunClient;


public class StunPeerReferenceFactory implements PeerReferenceFactory {

	private StunClient client;
	private int port;
	
	public StunPeerReferenceFactory(int port, InetSocketAddress stunServer) {
		Validate.isTrue(InetPeerReference.validatePort(port));
		this.port = port;
		
		Validate.notNull(stunServer);
		this.client = new StunClient(stunServer);
		this.localFactory = new LocalPeerReferenceFactory(port);
	}
	
	public PeerReference create() throws PeerReferenceFactoryException {
		InetPeerReference publicReference;
		
		try {
			publicReference = InetPeerReference.getInstance(client.getPublicSocketAddress(port));
		} catch (IOException e) {
			throw new PeerReferenceFactoryException("Can't request STUN server", e);
		} 

		return InetPeerReferences.create(localFactory.create(),publicReference);
	}

	private final LocalPeerReferenceFactory localFactory; 

}
