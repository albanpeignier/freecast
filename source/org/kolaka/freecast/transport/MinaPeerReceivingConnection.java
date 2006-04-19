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

package org.kolaka.freecast.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.kolaka.freecast.net.StunClient;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerReceivingConnection;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.peer.event.VetoPeerConnectionStatusChangeException;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.transport.cas.ConnectionAssistantClient;

public class MinaPeerReceivingConnection extends BaseMinaPeerConnection
		implements PeerReceivingConnection {

	private final SocketAddress address;
	private boolean localConnection;

	private final IoConnector connector;

	public MinaPeerReceivingConnection(SocketAddress address) {
		this(address, new DatagramConnector());
	}

	public MinaPeerReceivingConnection(SocketAddress address,
			IoConnector connector) {
		Validate.notNull(connector);
		this.connector = connector;
		this.address = address;
		this.localConnection = 
			isLocalAddress(address);  
	}

	private boolean isLocalAddress(SocketAddress socketAddress) {
		if (!(address instanceof InetSocketAddress)) {
			return false;
		}
		InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
		return inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress();
	}
	
	private boolean isLocalAddress(SocketAddress socketAddress, InetSocketAddress localAddress) {
		if (!(address instanceof InetSocketAddress)) {
			return false;
		}
		InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
		return inetAddress.equals(localAddress.getAddress());
	}


	private LatencyMonitor latencyMonitor = new LatencyMonitor();

	private ConnectionAssistantClient caClient;

	public long getLatency() {
		return latencyMonitor.getLatency();
	}

	private StunClient stunClient = StunClient.getDefaultInstance();

	private InetSocketAddress publicAddress;

	public void open() {
		LogFactory.getLog(getClass()).debug("open connection to " + address + " (local:" + localConnection + ")");
		connector.getFilterChain().addFirst("freecast",
				MinaProtocolCodecFactory.getFilter());

		int localPort = (int) (30000 + Math.random() * 10000.0);

		if (caClient != null && !localConnection) {
			try {
				publicAddress = stunClient.getPublicSocketAddress(localPort);
			} catch (IOException e) {
				LogFactory.getLog(getClass()).error("Can't perform STUN request", e);
			}
			localConnection = isLocalAddress(address, publicAddress);
		}

		IoHandler ioHandler = new IoHandlerAdapter() {

			public void sessionCreated(final IoSession session)
					throws Exception {
				LogFactory.getLog(getClass()).debug("session created");

				open(session);

				if (caClient != null && publicAddress != null &&  !localConnection) {
					Runnable task = new TimedLoopSender("status from "
							+ session.getLocalAddress() + " to " + address) {
						protected void loopStarted() {
							try {
								caClient.assist(publicAddress, address);
							} catch (Exception e) {
								LogFactory.getLog(getClass()).error(
										"Can't request assistance to connect "
												+ address, e);
							}
						}

						protected void send() {
							sendNodeStatus();
						}
					};
					getTimer().executeLater(task);
				} else {
					sendNodeStatus();
				}
			}

			public void sessionClosed(IoSession session) throws Exception {
				closeImpl();
			}

			public void exceptionCaught(IoSession session, Throwable t)
					throws Exception {
				if (getStatus().equals(PeerConnection.Status.OPENING)
						|| getStatus().equals(PeerConnection.Status.ACTIVATED)) {
					LogFactory.getLog(getClass()).error(
							"exception caught by handler", t);
					closeImpl();
				}
			}

			public void messageReceived(IoSession session, Object object)
					throws Exception {
				LogFactory.getLog(getClass()).trace(
						"message received " + object + " from "
								+ session.getRemoteAddress());
				Message message = (Message) object;
				processMessage(message);
			}

		};

		connector.connect(address, new InetSocketAddress(localPort),
				new NDCIoHandler(ioHandler));
	}

	protected void processMessage(Message message) throws IOException {
		if (message instanceof PeerStatusMessage) {
			PeerStatus remoteStatus = ((PeerStatusMessage) message)
					.getPeerStatus();
			if (!getStatus().equals(PeerConnection.Status.OPENING)) {
				latencyMonitor.statusReceived(remoteStatus);
			}
			firePeerStatus(remoteStatus);

			if (getStatus().equals(PeerConnection.Status.OPENING)) {
				try {
					changeStatus(PeerConnection.Status.OPENED);
				} catch (VetoPeerConnectionStatusChangeException e) {
					LogFactory.getLog(getClass()).debug(
							"can't open connection with "
									+ remoteStatus.getIdentifier(), e);
					closeImpl();
					return;
				}
			}
		} else if (message instanceof PeerConnectionStatusMessage) {
			PeerConnection.Status status = ((PeerConnectionStatusMessage) message)
					.getStatus();
			if (!status.equals(getStatus())) {
				LogFactory.getLog(getClass()).debug(
						"connection status accepted from remote: " + status);
				setStatus(status);
			}
		}

		super.processMessage(message);
	}

	public void activate() throws IOException {
		getWriter()
				.write(
						new PeerConnectionStatusMessage(
								PeerConnection.Status.ACTIVATED));
	}

	protected void sendNodeStatus(PeerStatus peerStatus) {
		if (!getStatus().equals(PeerConnection.Status.OPENING)) {
			latencyMonitor.statusSent(peerStatus);
		}
		super.sendNodeStatus(peerStatus);
	}

	protected Task createAliveTask() {
		return new Task() {
			public void run() {
				if (latencyMonitor.getMissingResponseCount() > 3) {
					LogFactory.getLog(getClass()).info(
							"No response from sender, connection closed");
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
				LogFactory.getLog(getClass()).trace(
						"Missing response from sender (" + missingResponseCount
								+ ")");
			}
			sendingTimeStamp = System.currentTimeMillis();
		}

		public int getMissingResponseCount() {
			return missingResponseCount;
		}

		public void statusReceived(PeerStatus remoteStatus) {
			if (sendingTimeStamp == UNKNOWN_TIMESTAMP) {
				LogFactory.getLog(getClass()).trace(
						"unexpected remote PeerStatus received: "
								+ remoteStatus);
				return;
			}
			long now = System.currentTimeMillis();
			latency = now - sendingTimeStamp;
			sendingTimeStamp = UNKNOWN_TIMESTAMP;
			missingResponseCount = 0;
			LogFactory.getLog(getClass()).trace("latency " + latency + " ms");
		}

		public long getLatency() {
			return latency;
		}

	}

	public void setConnectionAssistantClient(ConnectionAssistantClient client) {
		this.caClient = client;
	}

	protected void appendFields(ToStringBuilder builder) {
		super.appendFields(builder);
		builder.append("address", address);
		builder.append("latency", latencyMonitor.getLatency());
	}

}
