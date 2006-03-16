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

package org.kolaka.freecast.peer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.auditor.AuditorFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningListener;
import org.kolaka.freecast.peer.event.PeerConnectionStatusAdapter;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.PeerStatusEvent;
import org.kolaka.freecast.peer.event.PeerStatusListener;
import org.kolaka.freecast.peer.event.VetoPeerConnectionStatusChangeException;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionStatusListener;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Loop;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultPeerControler implements ConfigurablePeerControler,
		TimerUser {

	private final PeerStorage storage;
	private final PeerControler.Auditor auditor;

	public DefaultPeerControler() {
		ComparatorChain comparator = new ComparatorChain();
		comparator.addComparator(Peers.compareOrder());

		storage = new PeerStorage(comparator);
		auditor = (PeerControler.Auditor) AuditorFactory.getInstance().get(PeerControler.Auditor.class,
				this);
	}

	private PeerProvider provider;

	public void setPeerProvider(PeerProvider provider) {
		this.provider = provider;
	}

	private NodeStatusProvider statusProvider;

	public void setNodeStatusProvider(NodeStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}

	private PeerReceivingConnectionFactory factory;
	private Set factories = new HashSet();

	public void register(PeerConnectionFactory factory) {
		LogFactory.getLog(getClass()).debug("register " + factory);
		if (factory instanceof PeerReceivingConnectionFactory) {
			this.factory = (PeerReceivingConnectionFactory) factory;
		}
		
		this.factories.add(factory);
	}

	private Timer timer = DefaultTimer.getInstance();

	public void setTimer(Timer timer) {
		Validate.notNull(timer, "No specified Timer");
		this.timer = timer;
	}

	private final Predicate orderPredicate = new Predicate() {
		public boolean evaluate(Object o) {
			Peer peer = (Peer) o;

			Order order = statusProvider.getNodeStatus().getOrder();
			Order peerOrder = peer.getStatus().getOrder();

			return peerOrder != null && peerOrder.compareTo(order) <= 0;
		}
	};

	public List getBestPeers() throws NoPeerAvailableException {
		if (storage.isEmpty()) {
			updatePeers();
		}

		List peers = storage.find(orderPredicate);
		if (peers.isEmpty()) {
			throw new NoPeerAvailableException();
		}

		return peers;
	}

	protected void updatePeers() {
		LogFactory.getLog(getClass()).debug("retrieve new peer references");
		Set references;

		try {
			references = provider.getPeerReferences();
		} catch (PeerProviderException e) {
			LogFactory.getLog(getClass()).error(
					"Can't retrieve peer references", e);
			return;
		}

		for (Iterator iter = references.iterator(); iter.hasNext();) {
			PeerReference reference = (PeerReference) iter.next();
			PeerStatus status = getStatus(reference);
			MutablePeer peer = updatePeer(status);
			peer.setReference(reference);
		}

		storage.trim();

		LogFactory.getLog(getClass()).debug(storage.size() + " peers known");
	}
	
	private PeerStatus getStatus(PeerReference reference) {
		NodeIdentifier identifier = (NodeIdentifier) reference
		.getAttribute(PeerReference.IDENTIFIER_ATTRIBUTE);
		Order order = (Order) reference.getAttribute(PeerReference.ORDER_ATTRIBUTE);
		return new PeerStatus(identifier, order);
	}

	private MutablePeer updatePeer(PeerStatus status) {
		
		MutablePeer peer = storage.get(status.getIdentifier());

		if (peer == null) {
			LogFactory.getLog(getClass()).debug("new known peer: " + status);
			peer = new DefaultPeer(status);
			storage.add(peer);
		} else {
			peer.update(status);
		}
		
		return peer;
	}

	private final Loop updateLoop = new Loop() {
		public long loop() {
			updatePeers();
			// TODO make the loop delay variable
			return DefaultTimer.minutes(2);
		}
	};

	public void start() throws ControlException {
		if (statusProvider == null) {
			throw new IllegalStateException("No defined PeerStatusProvider");
		}

		for (Iterator iter=factories.iterator(); iter.hasNext(); ) {
			PeerConnectionFactory factory = (PeerConnectionFactory) iter.next();
			factory.add(openingListener);
		}

		if (factory != null) {
			factory.setStatusProvider(statusProvider);

			LogFactory.getLog(getClass()).trace("start asynchronous tasks");
			timer.execute(updateLoop);
		}
	}

	public void stop() throws ControlException {
		LogFactory.getLog(getClass()).debug("stopped");

		updateLoop.cancel();
	}

	/*
	 * deprecated methods
	 */

	public void init() throws ControlException {

	}

	public void dispose() throws ControlException {

	}

	private Map peerConnections = new HashMap();

	private void addPeerConnection(PeerConnection connection) throws VetoPeerConnectionStatusChangeException {
		LogFactory.getLog(getClass()).debug(
				"register the connection " + connection);

		NodeIdentifier peerIdentifier = connection.getPeerIdentifier();
		if (statusProvider.getNodeIdentifier().equals(peerIdentifier)) {
			throw new VetoPeerConnectionStatusChangeException("Cant' open connection with myself");
		}
		if (peerConnections.containsKey(peerIdentifier)) {
			throw new VetoPeerConnectionStatusChangeException("Connection already exists with " + peerIdentifier);
		}

		PeerStatus remoteStatus = connection.getRemoteStatus();
		if (connection instanceof PeerReceivingConnection && 
				remoteStatus.getOrder().isLower(statusProvider.getNodeStatus().getOrder())) {
			throw new VetoPeerConnectionStatusChangeException("Cant' receive from lower order peer: " + remoteStatus);
		}

		
		peerConnections.put(peerIdentifier, connection);
		
		auditor.acceptConnection(storage.get(peerIdentifier));
		auditor.connectionCount(peerConnections.size());
	}

	private void removePeerConnection(PeerConnection connection) {
		LogFactory.getLog(getClass()).debug(
				"unregister the connection " + connection);

		NodeIdentifier peerIdentifier;
		
		try {
			peerIdentifier = connection.getPeerIdentifier();
		} catch (IllegalStateException e) {
			// connection was never opened
			return;
		}

		peerConnections.remove(peerIdentifier);

		MutablePeer peer = storage.get(peerIdentifier);
		peer.setLatency(Peer.INFINITE_LATENCY);
		auditor.closeConnection(peer);
		auditor.connectionCount(peerConnections.size());
	}

	private final PeerConnectionOpeningListener openingListener = new PeerConnectionOpeningListener() {

		public void connectionOpening(PeerConnection connection) {
			connection.add(vetoConnectionListener);
			connection.add(connectionListener);
			connection.add(statusListener);
		}
		
	};
	
	private final VetoablePeerConnectionStatusListener vetoConnectionListener = new VetoablePeerConnectionStatusListener() {
		
		public void vetoablePeerConnectionStatusChange(PeerConnectionStatusEvent event) throws VetoPeerConnectionStatusChangeException {
			if (event.getStatus().equals(PeerConnection.Status.OPENED)) {
				addPeerConnection(event.getConnection());				
			}
		}
		
	};

	private final PeerConnectionStatusListener connectionListener = new PeerConnectionStatusAdapter() {
		
		protected void connectionClosed(PeerConnection connection) {
			removePeerConnection(connection);
		}

	};
	
	private final PeerStatusListener statusListener = new PeerStatusListener() {
		public void peerStatusChanged(PeerStatusEvent event) {
			PeerStatus status = event.getStatus();
			
			MutablePeer peer = updatePeer(status);
			
			if (event.getSource() instanceof PeerReceivingConnection) {
				PeerReceivingConnection connection = (PeerReceivingConnection) event.getSource();
				peer.setLatency(connection.getLatency());
			}
			
			LogFactory.getLog(getClass()).debug("updated peer: " + peer);
		
			for (Iterator iter = statusListeners.iterator(); iter.hasNext();) {
				PeerStatusListener listener = (PeerStatusListener) iter.next();
				listener.peerStatusChanged(event);
			}
		}
	};
	
	private Set statusListeners = new HashSet();

	public void add(PeerStatusListener listener) {
		statusListeners.add(listener);
	}

	public void remove(PeerStatusListener listener) {
		statusListeners.remove(listener);
	}

}