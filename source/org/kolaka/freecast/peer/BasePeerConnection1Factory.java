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
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.PeerStatusMessage;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class BasePeerConnection1Factory extends BasePeerConnectionFactory implements PeerConnectionFactory {

	public PeerConnection1 create(Peer peer, PeerReference reference)
			throws PeerConnectionFactoryException {
		LogFactory.getLog(getClass()).trace(
				"create a PeerConnection to " + peer + " by " + reference);

		PeerConnection1 connection = createImpl(peer, reference);
		connection.setPeer(peer);

		PeerConnectionFactoryException exception = null;

		try {
			init(connection);

			if (!connection.getStatus().equals(PeerConnection.Status.OPENED)) {
				throw new PeerConnectionFactoryException(
						"connection should be opened at this stage "
								+ connection);
			}
		} catch (PeerConnectionFactoryException e) {
			exception = e;
		} catch (Throwable t) {
			String msg = "connection initialization failed";
			exception = new PeerConnectionFactoryException(msg, t);
		} finally {
			if (exception != null) {
				LogFactory.getLog(getClass()).debug(
						"closed not opened connection " + connection);
				PeerConnections.closeQuietly(connection);

				throw exception;
			}
		}

		return connection;
	}

	private void init(PeerConnection1 connection)
			throws PeerConnectionFactoryException {
		LogFactory.getLog(getClass()).trace(
				"receive peer status via " + connection);
		PeerStatus peerStatus;

		try {
			PeerStatusMessage peerStatusMessage = (PeerStatusMessage) connection
					.getReader().read();
			peerStatus = peerStatusMessage.getPeerStatus();
		} catch (IOException e) {
			throw new PeerConnectionFactoryException(
					"Can't receive remote peer status", e);
		}

		LogFactory.getLog(getClass()).debug(
				"received " + peerStatus + " via " + connection);

		NodeStatus nodeStatus = getStatusProvider().getNodeStatus();

		try {
			PeerStatus localStatus = nodeStatus.createPeerStatus();

			LogFactory.getLog(getClass()).trace(
					"send node status " + localStatus + " via " + connection);

			connection.getWriter().write(new PeerStatusMessage(localStatus));
		} catch (IOException e) {
			throw new PeerConnectionFactoryException(
					"Can't send local peer status", e);
		}

		// TODO could be moved into a VetoPeerConnectionStatusListener
		if (nodeStatus.getIdentifier().equals(peerStatus.getIdentifier())) {
			String msg = "Can't accept a connection with myself";
			throw new PeerConnectionFactoryException(msg);
		}

		// TODO could be moved into a VetoPeerConnectionStatusListener
		if (nodeStatus.getOrder().compareTo(peerStatus.getOrder()) < 0) {
			String msg = "Can't accept a connection to a peer with a lower order"
					+ nodeStatus.getOrder() + "/" + peerStatus.getOrder();
			throw new PeerConnectionFactoryException(msg);
		}

		fireConnectionOpening(connection);

		// The status is changed to OPENED by the PeerConnectionSource
		try {
			Message message = connection.getReader().read();
			LogFactory.getLog(getClass()).trace(
					"connection status message: " + message);
		} catch (IOException e) {
			throw new PeerConnectionFactoryException(
					"Can't receive connection status", e);
		}

	}

	protected abstract PeerConnection1 createImpl(Peer peer,
			PeerReference reference) throws PeerConnectionFactoryException;

}