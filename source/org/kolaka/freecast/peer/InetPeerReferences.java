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

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class InetPeerReferences {
	
	public static MultiplePeerReference create(PeerReference reference, PeerReference additionnal) {
		if (reference instanceof MultiplePeerReference) {
			return create((MultiplePeerReference) reference, additionnal);
		}
		
		Set references = new HashSet();
		references.add(reference);
		references.add(additionnal);
		return new MultiplePeerReference(references);
	}
	
	public static MultiplePeerReference create(MultiplePeerReference reference, PeerReference additionnal) {
		Set references = new HashSet(reference.references());
		references.add(additionnal);
		return new MultiplePeerReference(references);
	}

	public static MultiplePeerReference create(Set inetAddresses, int port) {
		Set references = new HashSet();
		
		for (Iterator iter = inetAddresses.iterator(); iter.hasNext();) {
			InetAddress address = (InetAddress) iter.next();
			references.add(InetPeerReference.getInstance(address
					.getHostAddress(), port, false));
		}
		
		return new MultiplePeerReference(references);
	}
	
}
