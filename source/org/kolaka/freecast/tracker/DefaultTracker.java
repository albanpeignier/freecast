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

package org.kolaka.freecast.tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.auditor.AuditorFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.service.ControlException;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultTracker implements Tracker {

	private Map entries = new HashMap();

	private final Tracker.Auditor auditor;

	private final ClientInfoProvider clientInfoProvider;

	public DefaultTracker(ClientInfoProvider clientInfoProvider) {
		this.clientInfoProvider = clientInfoProvider;
		this.auditor = (Tracker.Auditor) AuditorFactory.getInstance().get(
				Tracker.Auditor.class, this);
	}

	public Set getPeerReferences(NodeIdentifier identifier) {
		cleanEntries();

		Set references = new HashSet();
		for (Iterator iter = entries.values().iterator(); iter.hasNext();) {
			NodeEntry entry = (NodeEntry) iter.next();
			if (!entry.hasReference()) {
				continue;
			}
			if (!entry.hasKnownOrder()) {
				continue;
			}

			if (!identifier.equals(entry.getIdentifier())) {
				references.add(entry.getReference());
			}
		}

		LogFactory.getLog(getClass()).trace(
				"provides " + references + " to " + identifier);
		return references;
	}

	/**
	 * 
	 */
	public NodeIdentifier register(PeerReference reference) {
		LogFactory.getLog(getClass()).debug(
				"receive registration request for " + reference);

		PeerReference validatedReference = null;
		try {
			validatedReference = validateReference(reference);
		} catch (TrackerException e) {
			LogFactory.getLog(getClass()).error("reference validation failed",
					e);
		}
		LogFactory.getLog(getClass()).debug(
				"validated reference " + validatedReference);

		if (validatedReference != null) {
			for (Iterator iter = entries.values().iterator(); iter.hasNext();) {
				NodeEntry entry = (NodeEntry) iter.next();
				if (!entry.hasReference()) {
					continue;
				}

				if (validatedReference.equals(entry.getReference())) {
					LogFactory.getLog(getClass()).debug(
							"new registration remplaces " + entry);
					iter.remove();
				}
			}
		}

		NodeIdentifier identifier = createIdentifier();
		entries.put(identifier, new NodeEntry(identifier, validatedReference));

		LogFactory.getLog(getClass()).debug(
				"registration performed with id " + identifier);

		auditor.register(validatedReference);
		auditor.connectedNodes(entries.size());

		return identifier;
	}

	public void unregister(NodeIdentifier identifier) {
		NodeEntry entry = (NodeEntry) entries.remove(identifier);
		if (entry != null) {
			LogFactory.getLog(getClass()).debug("unregister " + entry);
		} else {
			String clientHost = "unknown";
			try {
				clientHost = clientInfoProvider.getClientHost();
			} catch (TrackerException e) {
				LogFactory.getLog(getClass()).error(
						"Can't retrieve the client host", e);
			}
			String msg = "unregister for an unknown identifier " + identifier
					+ " from " + clientHost;
			LogFactory.getLog(getClass()).warn(msg);
		}
		auditor.unregister(entry.getReference());
		auditor.connectedNodes(entries.size());
	}

	private PeerReference validateReference(PeerReference reference)
			throws TrackerException {
		if (!(reference instanceof InetPeerReference)) {
			return reference;
		}

		InetPeerReference inetReference = (InetPeerReference) reference;
		InetAddress address = inetReference.getSocketAddress().getAddress();

		if (address == null || address.isAnyLocalAddress()) {
			String clientHost = clientInfoProvider.getClientHost();

			InetAddress clientAddress;

			try {
				clientAddress = InetAddress.getByName(clientHost);
			} catch (UnknownHostException e) {
				throw new TrackerException("Can't resolve " + clientHost, e);
			}

			return inetReference.specifyAddress(clientAddress);
		}

		return inetReference;
	}

	public void refresh(NodeStatus status) throws TrackerException.UnknownNode {
		NodeEntry entry = (NodeEntry) entries.get(status.getIdentifier());

		if (entry == null) {
			throw new TrackerException.UnknownNode(status.getIdentifier());
		}

		LogFactory.getLog(getClass()).trace("refresh " + status);
		entry.refresh(status);
		LogFactory.getLog(getClass()).trace("refreshed " + entry);
	}

	private final NodeIdentifierGenerator generator = new NodeIdentifierGenerator();

	protected NodeIdentifier createIdentifier() {
		return generator.next();
	}

	protected void cleanEntries() {
		Date oldestEntry = new Date(System.currentTimeMillis() - 60 * 1000 * 3);

		for (Iterator iter = entries.values().iterator(); iter.hasNext();) {
			NodeEntry entry = (NodeEntry) iter.next();

			if (oldestEntry.after(entry.getLastRefreshDate())) {
				LogFactory.getLog(getClass()).debug("remove " + entry);
				iter.remove();
				auditor.unregister(entry.getReference());
			}
		}
		auditor.connectedNodes(entries.size());
	}

	public void start() throws ControlException {
		entries.clear();
	}

	public void stop() throws ControlException {

	}

	class NodeEntry {

		private NodeIdentifier identifier;

		private PeerReference reference;

		private Date lastRefreshDate;

		private Order order;

		public NodeEntry(NodeIdentifier identifier, PeerReference reference) {
			this.identifier = identifier;
			this.reference = reference;
			this.lastRefreshDate = new Date();
		}

		public boolean equals(Object o) {
			return o instanceof NodeEntry && equals((NodeEntry) o);
		}

		public boolean equals(NodeEntry entry) {
			return entry != null && identifier.equals(entry.identifier);
		}

		public int hashCode() {
			return identifier.hashCode();
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		/**
		 * @return Returns the identifier.
		 */
		public NodeIdentifier getIdentifier() {
			return identifier;
		}

		/**
		 * @return Returns the reference.
		 */
		public PeerReference getReference() {
			if (reference == null) {
				return null;
			}

			reference.setAttribute(PeerReference.IDENTIFIER_ATTRIBUTE,
					identifier);
			reference.setAttribute(PeerReference.ORDER_ATTRIBUTE, order);
			return reference;
		}

		public boolean hasReference() {
			return reference != null;
		}

		public boolean hasKnownOrder() {
			return order != null && !order.equals(Order.UNKNOWN);
		}

		public void refresh(NodeStatus status) {
			lastRefreshDate = new Date();
			order = status.getOrder();
		}

		public Date getLastRefreshDate() {
			return lastRefreshDate;
		}
	}

	interface ClientInfoProvider {

		public String getClientHost() throws TrackerException;

	}

}