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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.peer.event.PeerStatusEvent;
import org.kolaka.freecast.peer.event.PeerStatusListener;

/**
 * 
 * @todo verify the equals and hashcode logics
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultPeer implements MutablePeer {

	private NodeIdentifier identifier;
	private PeerReference reference;
	private Order order;
	private long latency = INFINITE_LATENCY;

	private DefaultPeer() {
		order = Order.UNKNOWN;
	}

	public DefaultPeer(PeerStatus status) {
		update(status);
	}

	public PeerReference getReference() {
		return reference;
	}
	
	public void setReference(PeerReference reference) {
		this.reference = reference;
		Order referenceOrder = (Order) reference.getAttribute(PeerReference.ORDER_ATTRIBUTE);
		if (order == null && referenceOrder != null) {
			order = referenceOrder;
		}
	}

	public PeerStatus getStatus() {
		return new PeerStatus(identifier, order);
	}
	
	public long getLatency() {
		return latency;
	}
	
	public void setLatency(long latency) {
		this.latency = latency;
	}
	
	private void setIdentifier(NodeIdentifier identifier) {
		if (this.identifier != null && !this.identifier.equals(identifier)) {
			throw new IllegalStateException("Identifier already defined: "
					+ this.identifier + " and mismatchs " + identifier);
		}
		this.identifier = identifier;
	}

	private void setOrder(Order order) {
		Object old = this.order;
		this.order = order;
		if (!ObjectUtils.equals(old, order)) {
			LogFactory.getLog(getClass()).trace("fire order change: " + order);
			fireStatusChanged();
		}
	}

	public void update(PeerStatus peerStatus) {
		LogFactory.getLog(getClass()).trace("update " + peerStatus);

		setIdentifier(peerStatus.getIdentifier());
		setOrder(peerStatus.getOrder());
	}
	
	private Set listeners = new HashSet();
	
	public void add(PeerStatusListener listener) {
		listeners.add(listener);
	}
	
	public void remove(PeerStatusListener listener) {
		listeners.remove(listener);
	}
	
	private void fireStatusChanged() {
		PeerStatusEvent event = new PeerStatusEvent(this, getStatus());
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			PeerStatusListener listener = (PeerStatusListener) iter.next();
			listener.peerStatusChanged(event);
		}
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

}