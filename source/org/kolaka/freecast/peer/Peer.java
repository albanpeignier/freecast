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

import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.Order;

/**
 * 
 * @todo to be clean
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public interface Peer {

	PeerReference getReference();

	/**
	 * @return Returns the identifier.
	 */
	NodeIdentifier getIdentifier();

	public static final String ORDER_PROPERTYNAME = "order";

	Order getOrder();

	ConnectivityScoring getConnectivityScoring();

	void updateScoring();

	PeerStatus getStatus();

	PeerConnection connect() throws PeerConnectionFactoryException;

	public static final String CONNECTION_PROPERTYNAME = "connection";

	public PeerConnection getConnection();

	public boolean isConnected();

	public void disconnect();

	public void registerConnection(PeerConnection connection);

	void update(PeerStatus peerStatus);

	void update(PeerReference reference);

	public void add(PropertyChangeListener listener);

	public void remove(PropertyChangeListener listener);

}