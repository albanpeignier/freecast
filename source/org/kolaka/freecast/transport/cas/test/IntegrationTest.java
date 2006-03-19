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

package org.kolaka.freecast.transport.cas.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoConnector;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.easymock.MockControl;
import org.kolaka.freecast.test.BaseTestCase;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService;
import org.kolaka.freecast.transport.cas.ConnectionAssistantServiceSkeleton;
import org.kolaka.freecast.transport.cas.ConnectionAssistantServiceStub;
import org.kolaka.freecast.transport.cas.DefaultConnectionAssistantService;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.ConnectionHandler;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.Session;

public class IntegrationTest extends BaseTestCase {

	private static final int SERVICE_PORT = 2000;

	private InetAddress localhost;

	private SocketAddress serviceAddress;

	private ConnectionAssistantService connectionService;

	protected void setUp() throws java.lang.Exception {
		super.setUp();
		
		localhost = InetAddress.getLocalHost();
		serviceAddress = new InetSocketAddress(localhost, SERVICE_PORT);

		connectionService = new DefaultConnectionAssistantService();
		ConnectionAssistantServiceSkeleton skeleton = new ConnectionAssistantServiceSkeleton(
				connectionService);

		IoAcceptor acceptor = new SocketAcceptor();
		acceptor.getFilterChain().addFirst("log", new LoggingFilter());
		acceptor.bind(new InetSocketAddress(SERVICE_PORT), skeleton);
	}

	public void testRegister() throws java.lang.Exception {
		IoConnector connector = new SocketConnector();
		connector.getFilterChain().addFirst("log", new LoggingFilter());

		ConnectionAssistantService service = new ConnectionAssistantServiceStub(
				connector, serviceAddress);

		Session session = service.connect();
		int listenPort = (int) (30000 + Math.random() * 1000);
		InetSocketAddress listenAddress = createAddress(listenPort);
		InetSocketAddress targetAddress = createAddress(listenPort + 10);

		MockControl handlerControl = MockControl
				.createControl(ConnectionHandler.class);
		ConnectionHandler handler = (ConnectionHandler) handlerControl
				.getMock();

		handler.connectionRequested(targetAddress, listenAddress);

		handlerControl.replay();

		session.register(listenAddress, handler);

		Thread.sleep(1000);

		testAssist(listenAddress, targetAddress);

		Thread.sleep(1000);

		session.close();
		handlerControl.verify();
	}

	private void testAssist(InetSocketAddress sourceAddress,
			InetSocketAddress targetAddress) throws java.lang.Exception {
		IoConnector connector = new SocketConnector();
		connector.getFilterChain().addFirst("log", new LoggingFilter());

		ConnectionAssistantService service = new ConnectionAssistantServiceStub(
				connector, serviceAddress);

		Session session = service.connect();

		session.assist(sourceAddress, targetAddress);

		session.close();
	}

	private InetSocketAddress createAddress(int port) {
		return new InetSocketAddress(localhost, port);
	}

}
