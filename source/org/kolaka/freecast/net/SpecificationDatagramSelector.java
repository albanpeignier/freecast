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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Iterator;

public class SpecificationDatagramSelector implements SpecificationSelector {

	public InetSocketAddress select(InetSocketAddressSpecification specification) throws IOException {
		IOException lastException = null;

		for (Iterator iterator = specification.iterator(); iterator.hasNext();) {
			InetSocketAddress address = (InetSocketAddress) iterator.next();
			
			try {
				DatagramSocket socket = new DatagramSocket(address);
				socket.close();
				return address;
			} catch (SocketException e) {
				lastException = e;
			} 
		}
		
		if (lastException == null) {
			throw new IOException("No specified InetSocketAddress into "
					+ specification);
		}

		throw lastException;
		
	}

}
