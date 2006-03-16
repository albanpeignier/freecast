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

import java.io.IOException;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningListener;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningSupport;
import org.kolaka.freecast.peer.event.VetoPeerConnectionOpeningException;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionOpeningListener;
import org.kolaka.freecast.service.Startable;
import org.kolaka.freecast.transport.PeerStatusMessage;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class PeerConnectionSource implements Startable {

	private final PeerConnectionOpeningSupport support = new PeerConnectionOpeningSupport();

	private NodeStatusProvider statusProvider;

	protected void accept(PeerConnection1 connection) {
		LogFactory.getLog(getClass()).trace("connection acceptation begins");

		try {
			acceptImpl(connection);
		} finally {
			if (!connection.getStatus().equals(PeerConnection.Status.OPENED)) {
				LogFactory.getLog(getClass()).debug(
						"connection closed " + connection);
				PeerConnections.closeQuietly(connection);
			}
		}
	}

	private void acceptImpl(PeerConnection1 connection) {
		if (statusProvider == null) {
			throw new IllegalStateException("No defined NodeStatusProvider");
		}
		if (registry == null) {
			throw new IllegalStateException("No defined Registry");
		}

		PeerStatus status = statusProvider.getNodeStatus().createPeerStatus();

		LogFactory.getLog(getClass()).trace("send local status " + status);

		try {
			connection.getWriter().write(new PeerStatusMessage(status));

			LogFactory.getLog(getClass()).trace("read remoted status");
			// the PeerConnection process the receiver PeerStatusMessage
			connection.getReader().read();
		} catch (IOException e) {
			LogFactory.getLog(getClass()).error(
					"Connection initialization failed", e);
			return;
		}

		if (!connection.getStatus().equals(PeerConnection.Status.OPENING)) {
			// occurs when the connection is closed by the remote peer
			LogFactory.getLog(getClass()).error(
					"invalid connection status: " + connection);
			return;
		}

		registry.registry(connection);

		LogFactory.getLog(getClass()).trace(
				"fire the connection opening to the veto listeners");
		try {
			support.fireVetoableConnectionOpening(connection);
		} catch (VetoPeerConnectionOpeningException e) {
			LogFactory.getLog(getClass()).debug(
					"veto on connection opening " + connection, e);
			return;
		}

		LogFactory.getLog(getClass()).trace("opens connection");
		connection.open();

		LogFactory.getLog(getClass()).trace(
				"fire the connection opening to the classic listeners");
		support.fireConnectionOpening(connection);
	}

	public void add(PeerConnectionOpeningListener listener) {
		support.add(listener);
	}

	public void remove(PeerConnectionOpeningListener listener) {
		support.remove(listener);
	}

	public void add(VetoablePeerConnectionOpeningListener listener) {
		support.add(listener);
	}

	public void remove(VetoablePeerConnectionOpeningListener listener) {
		support.remove(listener);
	}

	public void setStatusProvider(NodeStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}

	private Registry registry;

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	/**
	 * Invokes to register connections into the <code>PeerControler</code>.
	 * 
	 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
	 */
	public static interface Registry {

		public void registry(PeerConnection connection);

	}

}
