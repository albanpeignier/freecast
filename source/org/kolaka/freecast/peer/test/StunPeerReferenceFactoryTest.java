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

package org.kolaka.freecast.peer.test;

import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.PeerReferenceFactoryException;
import org.kolaka.freecast.peer.StunPeerReferenceFactory;

public class StunPeerReferenceFactoryTest extends TestCase {

	public void testCreate() throws Exception {
		int port = (int) (30000 + Math.random() * 10000);
		PeerReference reference = testCreate(port, "stun.xten.net");
		assertEquals(reference, testCreate(port, "stun.fwdnet.net"));
	}

	/**
	 * @param port
	 * @param stunServer
	 * @return 
	 * @throws PeerReferenceFactoryException
	 */
	private PeerReference testCreate(int port, String stunServer) throws PeerReferenceFactoryException {
		StunPeerReferenceFactory factory = new StunPeerReferenceFactory(port, new InetSocketAddress(stunServer, 3478));
		
		PeerReference reference = factory.create();
		System.out.println(reference);

		assertEquals(reference, factory.create());
		return reference;
	}

}
