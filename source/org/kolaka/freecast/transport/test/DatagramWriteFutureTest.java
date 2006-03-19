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

package org.kolaka.freecast.transport.test;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.DatagramConnector;

import junit.framework.TestCase;

public class DatagramWriteFutureTest extends TestCase {

	public void testJoin() throws IOException, InterruptedException {
		// int port = (int) (20000 + 10000 * Math.random());
		int port = 20564;
		InetSocketAddress address = new InetSocketAddress("noemie.tryphon.org", port);
		byte[] data = new byte[450];

		IoAcceptor acceptor = new DatagramAcceptor();
		/*
		acceptor.bind(address, new IoHandlerAdapter() {
			public void messageReceived(IoSession session, Object message) throws Exception {
				System.out.print('+');
			}
		});*/
		
		IoConnector connector = new DatagramConnector();
		ConnectFuture connectFuture = connector.connect(address, new IoHandlerAdapter() {
			
		});
		connectFuture.join();
		IoSession session = connectFuture.getSession();
		for (int i=0; i < 100; i++) {
			System.out.print(".");
			WriteFuture writeFuture = session.write(ByteBuffer.wrap(data));
			// writeFuture.join();
			Thread.sleep(500);
		}
		System.out.println();
		
		System.out.println("ok");
	}
	
}
