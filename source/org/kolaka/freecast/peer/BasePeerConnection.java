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
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.lang.UnexpectedException;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.PeerConnectionStatusSupport;
import org.kolaka.freecast.peer.event.PeerStatusEvent;
import org.kolaka.freecast.peer.event.PeerStatusListener;
import org.kolaka.freecast.peer.event.VetoPeerConnectionStatusChangeException;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionStatusListener;

public abstract class BasePeerConnection implements PeerConnection {

	private PeerConnection.Status status = PeerConnection.Status.INITIAL;

	public Status getStatus() {
		return status;
	}

	protected void setStatus(Status status) {
		try {
			changeStatus(status);
		} catch (VetoPeerConnectionStatusChangeException e) {
			throw new UnexpectedException("Can't set status to " + status, e);
		}
	}
	
	protected void changeStatus(Status status) throws VetoPeerConnectionStatusChangeException {
		Validate.notNull(status);

		if (ObjectUtils.equals(status, this.status)) {
			return;
		}
		
		support.checkVetoStatus(status);

		LogFactory.getLog(getClass()).debug("set status " + status);

		this.status = status;
		support.fireStatus(status);
	}
	
	private final PeerConnectionStatusSupport support = new PeerConnectionStatusSupport(this);

	public void add(PeerConnectionStatusListener listener) {
		support.add(listener);
	}

	public void remove(PeerConnectionStatusListener listener) {
		support.remove(listener);
	}

	public void add(VetoablePeerConnectionStatusListener listener) {
		support.add(listener);
	}

	public void remove(VetoablePeerConnectionStatusListener listener) {
		support.remove(listener);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	private Set peerStatusListeners = new HashSet();
	
	public void add(PeerStatusListener listener) {
		peerStatusListeners.add(listener);
	}

	public void remove(PeerStatusListener listener) {
		peerStatusListeners.remove(listener);
	}
	
	private PeerStatus remoteStatus;
	
	public PeerStatus getRemoteStatus() {
		if (remoteStatus == null) {
			throw new IllegalStateException("No available PeerStatus");
		}
			
		return remoteStatus;
	}
	
	protected void firePeerStatus(PeerStatus status) {
		this.remoteStatus = status;
		
		NodeIdentifier remoteIdentifier = status.getIdentifier();
		if (this.peerIdentifier == null) {
			this.peerIdentifier = remoteIdentifier;
		} else {
			Validate.isTrue(this.peerIdentifier.equals(remoteIdentifier));
		}
		
		PeerStatusEvent event = new PeerStatusEvent(this, status);
		for (Iterator iter = peerStatusListeners.iterator(); iter.hasNext();) {
			PeerStatusListener listener = (PeerStatusListener) iter.next();
			listener.peerStatusChanged(event);
		}
	}

	private NodeIdentifier peerIdentifier;

	public NodeIdentifier getPeerIdentifier() {
		if (peerIdentifier == null) {
			throw new IllegalStateException("Peer identifier not available");
		}
		return peerIdentifier;
	}
	
}
