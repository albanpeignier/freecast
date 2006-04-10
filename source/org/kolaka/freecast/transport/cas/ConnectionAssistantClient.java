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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.kolaka.freecast.net.StunClient;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.PeerReferenceProcessor;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Startable;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.ConnectionHandler;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.Exception;
import org.kolaka.freecast.transport.cas.ConnectionAssistantService.Session;

public class ConnectionAssistantClient implements Startable {
	
	private SocketAddress serviceAddress;
	private Session session;
	
	public void setServiceAddress(SocketAddress serviceAddress) {
		Validate.notNull(serviceAddress);
		this.serviceAddress = serviceAddress;
	}
	
	private PeerReference nodeReference;
	
	public void setNodeReference(PeerReference nodeReference) {
		this.nodeReference = nodeReference;
	}

	public void start() throws ControlException {
		ConnectionAssistantService service = new ConnectionAssistantServiceStub(
				new SocketConnector(), serviceAddress);

		LogFactory.getLog(getClass()).info("connect to ConnectionAssistant at " + serviceAddress);

		try {
			session = service.connect();
		} catch (Exception e) {
			throw new ControlException("Can't connect to the ConnectionAssistant service at " + serviceAddress, e);
		}
	}
	
	public void register(ConnectionHandler handler) throws Exception {
		Validate.notNull(handler);
		LogFactory.getLog(getClass()).debug("register handler for " + nodeReference);
		
		RegistererProcessor processor = new RegistererProcessor(handler);
		processor.process(nodeReference);
		processor.checkException();
	}
	
	public void assist(InetSocketAddress publicAddress, SocketAddress remoteAddress) throws Exception {
		Validate.notNull(remoteAddress);
		Validate.notNull(publicAddress);
		/*
		AssistProcessor processor = new AssistProcessor(localPort, (InetSocketAddress) remoteAddress);
		processor.process(nodeReference);
		processor.checkException();
		*/
		LogFactory.getLog(getClass()).debug("requests assistance to connect " + remoteAddress + " from " + publicAddress);
		session.assist((InetSocketAddress) remoteAddress, publicAddress);
	}
	
	abstract class BasePeerReferenceProcessor extends PeerReferenceProcessor {
		
		public void checkException() throws Exception {
			java.lang.Exception exception = getException();
			if (exception instanceof Exception) {
				throw (Exception) exception;
			}
		}
		
	}
	
	class RegistererProcessor extends BasePeerReferenceProcessor {
	
		private final ConnectionHandler handler;

		public RegistererProcessor(ConnectionHandler handler) {
			Validate.notNull(handler);
			this.handler = handler;
		}

		protected void process(InetPeerReference reference) throws Exception {
			session.register(reference.getSocketAddress(), handler);
		}
		
	}
	
	class AssistProcessor extends BasePeerReferenceProcessor {
		
		private final int localPort;
		private final InetSocketAddress remoteAddress;
		
		public AssistProcessor(int localPort, InetSocketAddress remoteAddress) {
			this.localPort = localPort;
			this.remoteAddress = remoteAddress;
		}

		protected void process(InetPeerReference reference) throws java.lang.Exception {
			InetSocketAddress localAddress = new InetSocketAddress(reference.getSocketAddress().getAddress(), localPort);
			session.assist(remoteAddress, localAddress);
		}
		
	}
	
	public void stop() throws ControlException {
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
				throw new ControlException("Can't disconnect properly from the ConnectionAssistant service", e);
			}
		}
	}

}
