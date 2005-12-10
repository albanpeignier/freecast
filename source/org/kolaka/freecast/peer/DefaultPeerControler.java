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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.auditor.AuditorFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningListener;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.VetoPeerConnectionOpeningException;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionOpeningListener;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Loop;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;
import org.kolaka.freecast.transport.PeerStatusMessage;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultPeerControler implements ConfigurablePeerControler,
		TimerUser {

	private final PeerStorage storage;

	private final Auditor auditor;

	public DefaultPeerControler() {
		ComparatorChain comparator = new ComparatorChain();
		comparator.addComparator(Peers.compareConnectivityScoring());
		comparator.addComparator(Peers.compareOrder());

		storage = new PeerStorage(comparator);
		auditor = (Auditor) AuditorFactory.getInstance().get(Auditor.class,
				this);
	}

	public DefaultPeerControler(PeerProvider provider) {
		this();
		setPeerProvider(provider);
	}

	private PeerProvider provider;

	public void setPeerProvider(PeerProvider provider) {
		this.provider = provider;
	}

	private NodeStatusProvider statusProvider;

	public void setNodeStatusProvider(NodeStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}

	private PeerConnectionFactory factory;

	private PeerConnectionSource source;

	public void register(PeerConnectionFactory factory) {
		this.factory = factory;
	}

	public void register(PeerConnectionSource source) {
		this.source = source;
	}

	public PeerConnectionSource getPeerConnectionSource() {
		return source;
	}

	private Timer timer = DefaultTimer.getInstance();

	public void setTimer(Timer timer) {
		Validate.notNull(timer, "No specified Timer");
		this.timer = timer;
	}

	private final Predicate orderPredicate = new Predicate() {
		public boolean evaluate(Object o) {
			Peer peer = (Peer) o;

			if (peer.getConnectivityScoring().compareTo(
					ConnectivityScoring.UNREACHEABLE) <= 0) {
				return false;
			}

			Order order = statusProvider.getNodeStatus().getOrder();
			Order peerOrder = peer.getOrder();

			return peerOrder.compareTo(order) <= 0;
		}
	};

	public Peer getBestPeer() throws NoPeerAvailableException {
		if (storage.isEmpty()) {
			updatePeers();
		}

		Peer peer = storage.first(orderPredicate);
		if (peer == null) {
			throw new NoPeerAvailableException();
		}

		return peer;
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
			updatePeer(reference);
		}

		storage.trim();

		LogFactory.getLog(getClass()).debug(storage.size() + " peers known");
	}

	private void updatePeer(PeerReference reference) {
		NodeIdentifier identifier = (NodeIdentifier) reference
				.getAttribute(PeerReference.IDENTIFIER_ATTRIBUTE);
		Peer peer = storage.get(identifier);

		if (peer == null) {
			peer = createPeer(reference);
		} else {
			peer.update(reference);
		}
	}

	private Peer createPeer(PeerReference reference) {
		return initPeer(new DefaultPeer(reference));
	}

	private Peer createPeer(PeerStatus status) {
		return initPeer(new DefaultPeer(status));
	}

	private Peer initPeer(DefaultPeer peer) {
		LogFactory.getLog(getClass()).trace(
				"init peer, add " + listeners.size() + " listeners");

		peer.setConnectionFactory(factory);
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			peer.add((PropertyChangeListener) iter.next());
		}
		storage.add(peer);
		return peer;
	}

	private final Loop updateLoop = new Loop() {
		public long loop() {
			updatePeers();
			// TODO make the loop delay variable
			return DefaultTimer.minutes(2);
		}
	};

	private final Task scoringLoop = new Task() {
		public void run() {
			LogFactory.getLog(getClass()).debug("update peer scoring");
			for (Iterator iter = storage.peers(); iter.hasNext();) {
				Peer peer = (Peer) iter.next();
				peer.updateScoring();
			}
		}
	};

	private final Task sendStatusLoop = new Task() {
		public void run() {
			LogFactory.getLog(getClass()).debug("send local to peers");
			for (Iterator iter = storage.peers(); iter.hasNext();) {
				Peer peer = (Peer) iter.next();
				if (peer.isConnected()) {
					timer.executeLater(new SendStatusTask(peer));
				}
			}
		}
	};

	public void start() throws ControlException {
		if (statusProvider == null) {
			throw new IllegalStateException("No defined PeerStatusProvider");
		}

		if (source != null) {
			source.setStatusProvider(statusProvider);
			source.setRegistry(new PeerConnectionSource.Registry() {
				public void registry(PeerConnection connection) {
					registerConnection(connection);
				}
			});
			source.add((PeerConnectionOpeningListener) openingListener);
			source.add((VetoablePeerConnectionOpeningListener) openingListener);

			source.start();
		}

		if (factory != null) {
			factory.setStatusProvider(statusProvider);
			factory.add((PeerConnectionOpeningListener) openingListener);

			LogFactory.getLog(getClass()).trace("start asynchronous tasks");
			timer.execute(updateLoop);
			timer.executePeriodically(DefaultTimer.seconds(30), scoringLoop,
					false);
		}

		timer.executePeriodically(DefaultTimer.seconds(30), sendStatusLoop,
				false);
	}

	private void registerConnection(PeerConnection connection) {
		LogFactory.getLog(getClass()).debug(
				"register connection: " + connection);

		PeerStatus peerStatus = connection.getLastPeerStatus();
		NodeIdentifier identifier = peerStatus.getIdentifier();

		Peer peer = storage.get(identifier);
		if (peer == null) {
			LogFactory.getLog(getClass()).debug(
					"create new peer for " + peerStatus);
			peer = createPeer(peerStatus);
		}

		if (peer.isConnected()) {
			LogFactory.getLog(getClass()).warn(
					"Peer already connected: " + peer);
			peer.disconnect();
		}

		connection.setPeer(peer);
		peer.registerConnection(connection);
	}

	public void stop() throws ControlException {
		LogFactory.getLog(getClass()).debug("stopped");

		updateLoop.cancel();
		scoringLoop.cancel();
		sendStatusLoop.cancel();

		if (source != null) {
			source.stop();
		}
	}

	/*
	 * deprecated methods
	 */

	public void init() throws ControlException {

	}

	public void dispose() throws ControlException {

	}

	/**
	 * @todo can be moved into an AsynchronousMessageWriter
	 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
	 */
	class SendStatusTask extends Task {

		private final Peer peer;

		public void run() {
			if (peer.isConnected()) {
				LogFactory.getLog(getClass()).debug(
						"send local status to " + peer);

				PeerStatusMessage message = new PeerStatusMessage(
						statusProvider.getNodeStatus().createPeerStatus());
				try {
					peer.getConnection().getWriter().write(message);
				} catch (IOException e) {
					LogFactory.getLog(getClass()).error(
							"failed to send local status to " + peer, e);
				}
			}
		}

		public SendStatusTask(final Peer peer) {
			this.peer = peer;
		}

	}

	private Set listeners = new HashSet();

	public void addPeerListener(PropertyChangeListener listener) {
		listeners.add(listener);

		for (Iterator iter = storage.peers(); iter.hasNext();) {
			Peer peer = (Peer) iter.next();
			peer.add(listener);
		}
	}

	public void removePeerListener(PropertyChangeListener listener) {
		listeners.remove(listener);

		for (Iterator iter = storage.peers(); iter.hasNext();) {
			Peer peer = (Peer) iter.next();
			peer.remove(listener);
		}
	}

	private Set peerConnections = new HashSet();

	private void addPeerConnection(PeerConnection connection) {
		LogFactory.getLog(getClass()).debug(
				"register the connection " + connection);
		peerConnections.add(connection);
		connection.add(connectionListener);

		auditor.acceptConnection(connection.getPeer().getReference());
		auditor.connectionCount(peerConnections.size());
	}

	private void removePeerConnection(PeerConnection connection) {
		LogFactory.getLog(getClass()).debug(
				"unregister the connection " + connection);
		peerConnections.remove(connection);
		connection.remove(connectionListener);

		auditor.closeConnection(connection.getPeer().getReference());
		auditor.connectionCount(peerConnections.size());
	}

	private final SourceListener openingListener = new SourceListener();

	class SourceListener implements PeerConnectionOpeningListener,
			VetoablePeerConnectionOpeningListener {

		public void connectionOpening(PeerConnection connection) {
			addPeerConnection(connection);
		}

		public void vetoableConnectionOpening(PeerConnection connection)
				throws VetoPeerConnectionOpeningException {
			if (connection.getType().equals(PeerConnection.Type.RELAY)) {
				int relayCount = CollectionUtils.countMatches(peerConnections,
						PeerConnections.acceptType(PeerConnection.Type.RELAY));
				if (relayCount >= maximunRelayCount) {
					String msg = "Maximum relay count reachable";
					throw new VetoPeerConnectionOpeningException(msg,
							connection);
				}
			}
		}

	}

	private final PeerConnectionStatusListener connectionListener = new PeerConnectionStatusListener() {

		public void peerConnectionStatusChanged(PeerConnectionStatusEvent event) {
			if (event.getStatus().equals(PeerConnection.Status.CLOSED)) {
				PeerConnection connection = event.getConnection();
				removePeerConnection(connection);
			}

			LogFactory.getLog(getClass()).trace(
					"forward " + event + " to "
							+ connectionStatusListeners.size() + " listeners");
			for (Iterator iter = connectionStatusListeners.iterator(); iter
					.hasNext();) {
				PeerConnectionStatusListener listener = (PeerConnectionStatusListener) iter
						.next();
				listener.peerConnectionStatusChanged(event);
			}
		}

	};

	private int maximunRelayCount = 3;

	/**
	 * @param maximunRelayCount
	 *            The maximunRelayCount to set.
	 */
	public void setMaximunRelayCount(int maximunRelayCount) {
		this.maximunRelayCount = maximunRelayCount;
	}

	private Set connectionStatusListeners = new HashSet();

	public void add(PeerConnectionStatusListener listener) {
		connectionStatusListeners.add(listener);
	}

	public void remove(PeerConnectionStatusListener listener) {
		connectionStatusListeners.remove(listener);
	}

}