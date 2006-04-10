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

import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;


public class Main {

	public static void main(String args[]) throws Exception {
		int port = 20000;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		LogFactory.getLog(Main.class).info(
				"start the Connection Service on " + port);

		ConnectionAssistantService connectionService = new DefaultConnectionAssistantService();
		ConnectionAssistantServiceSkeleton skeleton = new ConnectionAssistantServiceSkeleton(
				connectionService);
		
		IoAcceptor acceptor = new SocketAcceptor();
		acceptor.getFilterChain().addFirst("log", new LoggingFilter());
		acceptor.bind(new InetSocketAddress(port), skeleton);
	}

}
