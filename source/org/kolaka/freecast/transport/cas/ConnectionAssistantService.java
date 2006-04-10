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

public interface ConnectionAssistantService {

	public Session connect() throws Exception;

	public interface Session {

		void register(InetSocketAddress localAddress, ConnectionHandler handler)
				throws Exception;

		void assist(InetSocketAddress remoteAddress,
				InetSocketAddress localAddress) throws Exception;

		void close() throws Exception;

	}

	public interface ConnectionHandler {

		void connectionRequested(InetSocketAddress sourceAddress,
				InetSocketAddress targetAddress);

	}

	public class Exception extends java.lang.Exception {

		private static final long serialVersionUID = 5484103082803972814L;

		public Exception(String message, Throwable cause) {
			super(message, cause);
		}

		public Exception(String message) {
			super(message);
		}

	}

}
