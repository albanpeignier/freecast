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

package org.kolaka.freecast.transport;

import java.io.IOException;
import java.net.SocketAddress;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoFuture;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoFuture.Callback;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerReceivingConnection;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.peer.event.VetoPeerConnectionStatusChangeException;
import org.kolaka.freecast.timer.Task;

public class MinaPeerReceivingConnection extends BaseMinaPeerConnection implements PeerReceivingConnection {

	private final SocketAddress address;
	private final IoConnector connector;

	public MinaPeerReceivingConnection(SocketAddress address) {
		this(address, new DatagramConnector());
	}
	
	public MinaPeerReceivingConnection(SocketAddress address, IoConnector connector) {
		Validate.notNull(connector);
		this.connector = connector;
		this.address = address;
	}
	
	private LatencyMonitor latencyMonitor = new LatencyMonitor();
	
	public long getLatency() {
		return latencyMonitor.getLatency();
	}
	
	public void open() {
		LogFactory.getLog(getClass()).debug("open " + this);
		connector.getFilterChain().addFirst("freecast", MinaProtocolCodecFactory.getFilter());

		IoHandler ioHandler = new IoHandlerAdapter() {
			
			public void sessionCreated(IoSession session) throws Exception {
				LogFactory.getLog(getClass()).debug("session created");
				open(session);
				sendNodeStatus();
			}
			
			public void sessionClosed(IoSession session) throws Exception {
				closeImpl();
			}
			
			public void exceptionCaught(IoSession session, Throwable t) throws Exception {
				LogFactory.getLog(getClass()).error("exception caught by handler", t);
				closeImpl();
			}

			public void messageReceived(IoSession session, Object object) throws Exception {
				Message message = (Message) object;

				NDC.push(">" + address);

				try {
					processMessage(message);
				} finally {
					NDC.pop();
				}
			}
			
		};
		ConnectFuture connectFuture = connector.connect(address, ioHandler);
		Callback callback = new Callback() {

			public void operationComplete(IoFuture future) {
				try {
					((ConnectFuture) future).getSession();
				} catch (IOException e) {
					LogFactory.getLog(getClass()).debug("Can't connect to " + address, e);
					closeImpl();
				}
			}

		};
		connectFuture.setCallback(callback);
	}
	
	protected void processMessage(Message message) throws IOException {
		LogFactory.getLog(getClass()).trace("message received " + message);
		
		if (message instanceof PeerStatusMessage) {
			PeerStatus remoteStatus = ((PeerStatusMessage) message).getPeerStatus();
			latencyMonitor.statusReceived(remoteStatus);
			firePeerStatus(remoteStatus);
			
			if (getStatus().equals(PeerConnection.Status.OPENING)) {
				try {
					changeStatus(PeerConnection.Status.OPENED);
				} catch (VetoPeerConnectionStatusChangeException e) {
					LogFactory.getLog(getClass()).debug("can't open connection with " + remoteStatus.getIdentifier(), e);
					closeImpl();
					return;
				}
			}
		} else if (message instanceof PeerConnectionStatusMessage) {
			PeerConnection.Status status = ((PeerConnectionStatusMessage) message).getStatus();
			if (!status.equals(getStatus())) {
				LogFactory.getLog(getClass()).debug("connection status accepted from remote: " + status);
				setStatus(status);
			}
		}
		
		super.processMessage(message);
	}
	
	public void activate() throws IOException {
		getWriter().write(new PeerConnectionStatusMessage(PeerConnection.Status.ACTIVATED));
	}

	protected void sendNodeStatus(PeerStatus peerStatus) {
		latencyMonitor.statusSent(peerStatus);
		super.sendNodeStatus(peerStatus);
	}
	
	protected Task createAliveTask() {
		return new Task() {
			public void run() {
				if (latencyMonitor.getMissingResponseCount() > 3) {
					LogFactory.getLog(getClass()).info("No response from sender, connection closed");
					closeImpl();
				}

				sendNodeStatus();
			}
		};
	}

	class LatencyMonitor {
		
		private final static long UNKNOWN_TIMESTAMP = -1;
		private long latency = Long.MAX_VALUE;
		private long sendingTimeStamp = UNKNOWN_TIMESTAMP;
		private int missingResponseCount = 0;
		
		public void statusSent(PeerStatus status) {
			if (sendingTimeStamp != UNKNOWN_TIMESTAMP) {
				missingResponseCount++;
				LogFactory.getLog(getClass()).debug("Missing response from sender (" + missingResponseCount + ")");
			}
			sendingTimeStamp = System.currentTimeMillis();
		}
		
		public int getMissingResponseCount() {
			return missingResponseCount;
		}
		
		public void statusReceived(PeerStatus remoteStatus) {
			if (sendingTimeStamp == UNKNOWN_TIMESTAMP) {
				LogFactory.getLog(getClass()).debug("unexpected remote PeerStatus received: " + remoteStatus);
				return;
			}
			long now = System.currentTimeMillis();
			latency = now - sendingTimeStamp;
			sendingTimeStamp = UNKNOWN_TIMESTAMP;
			missingResponseCount = 0;
			LogFactory.getLog(getClass()).debug("latency " + latency + " ms");
		}
		
		public long getLatency() {
			return latency;
		}
		
	}
	
}
