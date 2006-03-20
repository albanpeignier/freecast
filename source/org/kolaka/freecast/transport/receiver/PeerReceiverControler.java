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

package org.kolaka.freecast.transport.receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.packet.signer.DummyPacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidatorUser;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.MultiplePeerReference;
import org.kolaka.freecast.peer.NoPeerAvailableException;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnectionFactoryException;
import org.kolaka.freecast.peer.PeerConnectionFuture;
import org.kolaka.freecast.peer.PeerConnections;
import org.kolaka.freecast.peer.PeerControler;
import org.kolaka.freecast.peer.PeerReceivingConnection;
import org.kolaka.freecast.peer.PeerReceivingConnectionFactory;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.Peers;
import org.kolaka.freecast.peer.event.PeerConnectionStatusAdapter;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.pipe.Pipe;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Controlables;
import org.kolaka.freecast.service.Service;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerReceiverControler implements ReceiverControler, TimerUser,
		PacketValidatorUser {

	private Pipe pipe;

	public void setPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	private PeerControler peerControler;

	private final Task startReceiverTask = new Task() {
		public void run() {
			long retryDelay = -1;

			try {
				createReceiver();
			} catch (NoPeerAvailableException e) {
				LogFactory.getLog(getClass()).debug("no peer available");
				retryDelay = DefaultTimer.seconds(5);
			} catch (Throwable e) {
				LogFactory.getLog(getClass()).error("receiver creation failed",
						e);
				retryDelay = DefaultTimer.seconds(3);
			}

			if (retryDelay > -1) {
				startReceiver(retryDelay);
			}
		}
	};

	private Service.Listener listener = new Service.Adapter() {
		public void serviceStopped(Service service) {
			if (ObjectUtils.equals(receiver, service)) {
				disposeReceiver();
			}

			service.remove(this);
		}
	};

	private PacketValidator packetValidator = new DummyPacketValidator();

	public void setPacketValidator(PacketValidator packetValidator) {
		Validate.notNull(packetValidator);
		this.packetValidator = packetValidator;
	}

	private Map openedConnections = new HashMap();
	
	private final Task openConnectionsTask = new Task() {
		public void run() {
			openConnections();
		}
	};

	private void openConnections() {
		List peers;
		try {
			peers = peerControler.getBestPeers();
		} catch (NoPeerAvailableException e) {
			LogFactory.getLog(getClass()).debug("no peer available");
			return;
		}

		LogFactory.getLog(getClass()).debug(
				"try to open connections with " + peers.size() + " known peers");

		for (Iterator iter=peers.iterator(); iter.hasNext(); ) {
			final Peer peer = (Peer) iter.next();
			if (openedConnections.containsKey(peer)) {
				PeerConnection connection = (PeerConnection) openedConnections.get(peer);
				LogFactory.getLog(getClass()).trace(
						"keep existing connection " + connection);
				continue;
			}
	
			LogFactory.getLog(getClass()).debug(
					"try to open a connection with " + peer);
			
			try {
				final PeerConnectionStatusListener closeListener = new PeerConnectionStatusAdapter() {
					protected void connectionClosed(PeerConnection connection) {
						LogFactory.getLog(getClass()).debug("lost connection with " + peer);
						openedConnections.remove(peer);
					}
				};
				PeerConnectionStatusListener listener = new PeerConnectionStatusAdapter() {
					private boolean registered;
					
					protected synchronized void connectionOpened(PeerConnection connection) {
						if (registered) {
							LogFactory.getLog(getClass()).warn("several connections opened with " + peer);
							PeerConnections.closeQuietly(connection);
							return;
						}
						registered = true;
						LogFactory.getLog(getClass()).debug("new opened connection with " + peer);
						connection.add(closeListener);
						openedConnections.put(peer, connection);
					}
				};
				openConnections(peer, listener);
			} catch (PeerConnectionFactoryException e) {
				LogFactory.getLog(getClass()).debug("can't open connection with " + peer, e);
			}
		}
	}
	
	


	private PeerReceiver receiver;

	/**
	 * @todo review the exception handling
	 * 
	 * @throws NoPeerAvailableException
	 * @throws PeerConnectionFactoryException
	 * @throws ControlException
	 */
	private void createReceiver() throws NoPeerAvailableException {
		if (receiver != null) {
			throw new IllegalStateException("Receiver already exists: "
					+ receiver);
		}
		
		List bestConnectedPeers = new ArrayList(openedConnections.keySet());
		Collections.sort(bestConnectedPeers, peerComparator);

		for (Iterator iter=bestConnectedPeers.iterator(); iter.hasNext() && receiver == null; ) {
			Peer peer = (Peer) iter.next();
	
			LogFactory.getLog(getClass()).debug(
					"try to activate connection with " + peer);
			
			try {
				PeerReceivingConnection peerConnection = (PeerReceivingConnection) openedConnections.get(peer);
				if  (peerConnection == null) {
					continue;
				}
				
				peerConnection.activate();

				new PeerConnectionFuture(peerConnection).wait(PeerConnection.Status.ACTIVATED, 1000);

				LogFactory.getLog(getClass()).debug(
						"create a peer receiver for " + peerConnection);
				PeerReceiver receiver = new PeerReceiver(peerConnection);
				receiver.setPacketValidator(packetValidator);
				receiver.setProducer(pipe.createProducer());

				receiver.add(listener);

				LogFactory.getLog(getClass()).debug("start peer receiver " + receiver);
				Controlables.start(receiver);
				
				this.receiver = receiver;
			} catch (Exception e) {
				LogFactory.getLog(getClass()).debug("Can't open connection with " + peer, e);
			}
		}
		
		if (receiver == null) {
			throw new NoPeerAvailableException();
		}
	}
	
	private void openConnections(Peer peer, PeerConnectionStatusListener listener) throws PeerConnectionFactoryException {
		openConnections(peer, peer.getReference(), listener);
	}

	private void openConnections(Peer peer, PeerReference reference, PeerConnectionStatusListener listener) throws PeerConnectionFactoryException {
		if (reference instanceof MultiplePeerReference) {
			Set references = ((MultiplePeerReference) reference).references();

			for (Iterator iter = references.iterator(); iter.hasNext();) {
				PeerReference subReference = (PeerReference) iter.next();
				try {
					openConnections(peer, subReference, listener);
				} catch (PeerConnectionFactoryException e) {
					LogFactory.getLog(getClass()).debug("Can't connection " + peer + " on " + reference, e);
				}
			}
		} else if (reference instanceof InetPeerReference) {
			PeerReceivingConnection peerConnection;
			peerConnection = connectionFactory.create(peer, reference);
			peerConnection.add(listener);
			peerConnection.open();
		} else { 
			throw new IllegalArgumentException("Unsupport reference type: " + reference);
		}
	}
	
	private Timer timer = DefaultTimer.getInstance();

	public void setTimer(Timer timer) {
		Validate.notNull(timer, "No specified Timer");
		this.timer = timer;
	}

	private void startReceiver() {
		LogFactory.getLog(getClass()).debug("start a new receiver");
		timer.executeLater(startReceiverTask);
	}

	private void startReceiver(long delay) {
		LogFactory.getLog(getClass()).debug(
				"start a new receiver in " + delay + " ms");
		timer.executeAfterDelay(delay, startReceiverTask);
	}

	private void disposeReceiver() {
		LogFactory.getLog(getClass()).debug(
				"dispose current receiver " + receiver);
		receiver = null;
		if (!stopped) {
			startReceiver();
		}
	}

	public void start() throws ControlException {
		LogFactory.getLog(getClass()).debug("start");
		startReceiver();
		timer.executePeriodically(DefaultTimer.seconds(60), openConnectionsTask, true);
	}

	private boolean stopped;

	private PeerReceivingConnectionFactory connectionFactory;

	private ComparatorChain peerComparator;

	public void stop() throws ControlException {
		stopped = true;
		if (receiver != null) {
			receiver.stop();
		}
	}

	public void dispose() throws ControlException {

	}

	public void init() throws ControlException {

	}

	public PeerReceiverControler(PeerControler peerControler, PeerReceivingConnectionFactory connectionFactory) {
		this.peerControler = peerControler;
		this.connectionFactory = connectionFactory;
		
		ComparatorChain comparator = new ComparatorChain();
		comparator.addComparator(Peers.compareLatency());
		comparator.addComparator(Peers.compareOrder());
		this.peerComparator = comparator;

	}
	
	public ReceiverConfiguration getReceiverConfiguration() {
		return receiver.getReceiverConfiguration();
	}
	
}