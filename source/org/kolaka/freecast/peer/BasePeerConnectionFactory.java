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

package org.kolaka.freecast.peer;

import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningListener;
import org.kolaka.freecast.peer.event.PeerConnectionOpeningSupport;
import org.kolaka.freecast.peer.event.VetoPeerConnectionOpeningException;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionOpeningListener;

public abstract class BasePeerConnectionFactory implements PeerConnectionFactory {

	private NodeStatusProvider statusProvider;

	public void setStatusProvider(NodeStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}
	
	public NodeStatusProvider getStatusProvider() {
		if (statusProvider == null) {
			throw new IllegalStateException("No defined NodeStatusProvider");
		}
		return statusProvider;
	}

	private final PeerConnectionOpeningSupport support = new PeerConnectionOpeningSupport();

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
	
	protected void fireVetoableConnectionOpening(PeerConnection connection) throws VetoPeerConnectionOpeningException {
		support.fireVetoableConnectionOpening(connection);
	}

	protected void fireConnectionOpening(PeerConnection connection) {
		support.fireConnectionOpening(connection);
	}

}
