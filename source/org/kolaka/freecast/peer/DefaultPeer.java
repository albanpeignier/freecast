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
import java.beans.PropertyChangeSupport;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;

/**
 * 
 * @todo verify the equals and hashcode logics
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultPeer implements Peer {

	/**
	 * Identifies of the <code>Node</code> designed by this <code>Peer</code>.
	 * Can be null if unknown.
	 */
	private NodeIdentifier identifier;

	private PeerReference reference;

	private Order order;

	private ConnectivityScoring connectivityScoring;

	private DefaultPeer() {
		connectivityScoring = ConnectivityScoring.UNKNOWN;
		order = Order.UNKNOWN;
	}

	public DefaultPeer(PeerReference reference) {
		this();
		update(reference);
	}

	public DefaultPeer(PeerStatus status) {
		this();
		update(status);
	}

	public PeerReference getReference() {
		return reference;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public boolean equals(Object o) {
		return o instanceof Peer && equals((DefaultPeer) o);
	}

	public boolean equals(DefaultPeer other) {
		if (identifier != null) {
			return ObjectUtils.equals(identifier, other.identifier);
		}

		return ObjectUtils.equals(reference, other.reference);
	}

	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		if (identifier != null) {
			builder.append(identifier);
		} else {
			builder.append(reference);
		}
		return builder.toHashCode();
	}

	/**
	 * @return Returns the identifier.
	 */
	public NodeIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 *            The identifier to set.
	 */
	protected void setIdentifier(NodeIdentifier identifier) {
		if (this.identifier != null && !this.identifier.equals(identifier)) {
			throw new IllegalStateException("Identifier already defined: "
					+ this.identifier + " and mismatchs " + identifier);
		}
		this.identifier = identifier;
	}

	public Order getOrder() {
		return order;
	}

	protected void setOrder(Order order) {
		Object old = this.order;
		this.order = order;
		if (!ObjectUtils.equals(old, order)) {
			LogFactory.getLog(getClass()).trace("fire order change: " + order);
			changeSupport.firePropertyChange(ORDER_PROPERTYNAME, old, order);
		}
	}

	protected void setReference(PeerReference reference) {
		this.reference = reference;
	}

	public ConnectivityScoring getConnectivityScoring() {
		return connectivityScoring;
	}

	protected void changeConnectivityScoring(ConnectivityScoring.Change change) {
		ConnectivityScoring previous = connectivityScoring;
		connectivityScoring = change.change(connectivityScoring);

		LogFactory.getLog(getClass()).debug(
				"change connectivity scoring " + change + ": " + previous
						+ " -> " + connectivityScoring);
	}

	public void updateScoring() {
		ConnectivityScoring.Change change = isConnected() ? ConnectivityScoring.BONUS_CONNECTIONTRAFFIC
				: ConnectivityScoring.IDLE;
		changeConnectivityScoring(change);
	}

	public PeerStatus getStatus() {
		return new PeerStatus(identifier, order);
	}

	private PeerConnectionFactory connectionFactory;

	private PeerConnection connection;

	private PeerConnectionStatusListener connectionListener = new PeerConnectionStatusListener() {
		public void peerConnectionStatusChanged(PeerConnectionStatusEvent event) {
			LogFactory.getLog(getClass()).trace("receive " + event);
			if (event.getStatus().equals(PeerConnection.Status.CLOSED)) {
				disconnect();
			}
		}
	};

	public PeerConnection connect() throws PeerConnectionFactoryException {
		if (connection == null) {
			PeerConnection old = connection;

			LogFactory.getLog(getClass()).debug("create connection");
			try {
				connection = createConnection();
			} catch (PeerConnectionFactoryException e) {
				changeConnectivityScoring(ConnectivityScoring.MALUS_CONNECTIONERROR);
				throw e;
			}

			LogFactory.getLog(getClass()).trace(
					"fire new connection: " + connection);
			changeSupport.firePropertyChange(CONNECTION_PROPERTYNAME, old,
					connection);

			connection.add(connectionListener);
		}

		changeConnectivityScoring(ConnectivityScoring.BONUS_CONNECTIONOPENED);

		return connection;
	}

	protected PeerConnection createConnection()
			throws PeerConnectionFactoryException {
		if (connectionFactory == null) {
			throw new IllegalStateException("No defined PeerConnectionFactory");
		}

		return connectionFactory.create(this, reference);
	}

	public PeerConnection getConnection() {
		if (connection == null) {
			throw new IllegalStateException("No available connection");
		}
		return connection;
	}

	/**
	 * @deprecated find a better way to associated opened PeerConnection and
	 *             Peer
	 * @param connection
	 */
	public void registerConnection(PeerConnection connection) {
		LogFactory.getLog(getClass())
				.debug("register connection " + connection);

		if (isConnected()) {
			throw new IllegalStateException("Peer already connected: " + this);
		}

		Validate.isTrue(connection.getStatus().equals(
				PeerConnection.Status.OPENING), "connection shoud be opening",
				connection);
		Validate
				.isTrue(ObjectUtils.equals(connection.getPeer(), this),
						"connection shoud associated to this Peer: " + this,
						connection);

		connection.add(connectionListener);
		this.connection = connection;
	}

	public void disconnect() {
		if (connection != null) {
			boolean isClosing = connection.getStatus().equals(
					PeerConnection.Status.CLOSING);
			ConnectivityScoring.Change change = isClosing ? ConnectivityScoring.MALUS_CONNECTIONCLOSED
					: ConnectivityScoring.MALUS_CONNECTIONERROR;
			changeConnectivityScoring(change);

			connection.remove(connectionListener);
			connection = null;
		}
	}

	/**
	 * @todo should be transformed into a state
	 * @return
	 */
	public boolean isConnected() {
		return connection != null;
	}

	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
			this);

	public void add(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void remove(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	public void update(PeerStatus peerStatus) {
		LogFactory.getLog(getClass()).debug("update " + peerStatus);

		setIdentifier(peerStatus.getIdentifier());
		setOrder(peerStatus.getOrder());
	}

	public void update(PeerReference reference) {
		setReference(reference);

		NodeIdentifier identifier = (NodeIdentifier) reference
				.getAttribute(PeerReference.IDENTIFIER_ATTRIBUTE);
		if (identifier != null) {
			setIdentifier(identifier);
		}

		Order order = (Order) reference
				.getAttribute(PeerReference.ORDER_ATTRIBUTE);
		if (order != null) {
			setOrder(order);
		}
	}

	public void setConnectionFactory(PeerConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

}