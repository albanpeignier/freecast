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

package org.kolaka.freecast.transport.cas.test;

import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService;
import org.kolaka.freecast.transport.cas.DefaultConnectionAssistantService;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.ConnectionHandler;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.Exception;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.Session;

public class DefaultConnectionAssistantServiceTest extends TestCase {

	private ConnectionAssistantService service;

	private InetSocketAddress acceptorAddress, connectorAddress;

	private MockControl handlerControl;

	private ConnectionHandler handler;

	protected void setUp() throws Exception {
		service = new DefaultConnectionAssistantService();
		acceptorAddress = InetSocketAddress.createUnresolved("acceptor", 1000);
		connectorAddress = InetSocketAddress
				.createUnresolved("connector", 1000);
		handlerControl = MockControl.createControl(ConnectionHandler.class);
		handler = (ConnectionHandler) handlerControl.getMock();
	}

	public void testNormalAssistance() throws Exception {
		handler.connectionRequested(connectorAddress, acceptorAddress);
		handlerControl.replay();

		Session acceptorSession = service.connect();
		acceptorSession.register(acceptorAddress, handler);

		Session connectorSession = service.connect();
		connectorSession.assist(acceptorAddress, connectorAddress);

		handlerControl.verify();
	}

	public void testUnknownTarget() throws Exception {
		Session connectorSession = service.connect();

		try {
			connectorSession.assist(acceptorAddress, connectorAddress);
			fail("Assistance should fail");
		} catch (Exception e) {

		}
	}

	public void testClosedTargetSession() throws Exception {
		handler.connectionRequested(connectorAddress, acceptorAddress);
		handlerControl.replay();

		Session acceptorSession = service.connect();
		acceptorSession.register(acceptorAddress, handler);

		Session connectorSession = service.connect();
		connectorSession.assist(acceptorAddress, connectorAddress);

		acceptorSession.close();

		try {
			connectorSession.assist(acceptorAddress, connectorAddress);
			fail("Assistance should fail");
		} catch (Exception e) {

		}

		handlerControl.verify();
	}

	public void testClosedSession() throws Exception {
		Session connectorSession = service.connect();
		connectorSession.close();

		try {
			connectorSession.assist(acceptorAddress, connectorAddress);
			fail("Assistance should fail");
		} catch (Exception e) {

		}

		try {
			connectorSession.register(connectorAddress, handler);
			fail("Assistance should fail");
		} catch (Exception e) {

		}
	}

}
