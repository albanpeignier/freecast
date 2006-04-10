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

package org.kolaka.freecast.transport.cas;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

public class DefaultConnectionAssistantService implements
		ConnectionAssistantService {

	public Session connect() {
		LogFactory.getLog(getClass()).info("new session");
		return new SessionImpl();
	}

	private final Map handlers = new HashMap();

	void addHandler(InetSocketAddress localAddress, ConnectionHandler handler) {
		handlers.put(localAddress, handler);
	}

	ConnectionHandler getHandler(InetSocketAddress remoteAddress) {
		ConnectionHandler handler = (ConnectionHandler) handlers
				.get(remoteAddress);
		if (handler == null) {
			LogFactory.getLog(getClass()).debug("known addresses: " + handlers.keySet());
			throw new NoSuchElementException("No handler registered for "
					+ remoteAddress);
		}
		return handler;
	}

	void removeHandler(InetSocketAddress localAddress) {
		handlers.remove(localAddress);
	}

	public class SessionImpl implements Session {

		private boolean closed = false;

		private final Set registeredAddresses = new HashSet();

		protected void checkClosed() throws Exception {
			if (closed) {
				throw new Exception("Session already closed");
			}
		}

		public void assist(InetSocketAddress remoteAddress,
				InetSocketAddress localAddress) throws Exception {
			checkClosed();
			LogFactory.getLog(getClass()).info(
					"connection assistance asked for " + remoteAddress
							+ " from " + localAddress);

			try {
				getHandler(remoteAddress).connectionRequested(localAddress,
						remoteAddress);
			} catch (NoSuchElementException e) {
				throw new Exception("Unknown target address: " + remoteAddress,
						e);
			}
		}

		public void close() {
			LogFactory.getLog(getClass()).info("close");
			closed = true;
			for (Iterator iter = registeredAddresses.iterator(); iter.hasNext();) {
				InetSocketAddress address = (InetSocketAddress) iter.next();
				removeHandler(address);
			}
		}

		public void register(InetSocketAddress localAddress,
				ConnectionHandler handler) throws Exception {
			checkClosed();
			LogFactory.getLog(getClass()).info(
					"registration from " + localAddress);

			addHandler(localAddress, handler);
			registeredAddresses.add(localAddress);
		}

	}

}
