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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.lang.UnexpectedException;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.PeerConnectionStatusSupport;
import org.kolaka.freecast.peer.event.PeerStatusEvent;
import org.kolaka.freecast.peer.event.PeerStatusListener;
import org.kolaka.freecast.peer.event.VetoPeerConnectionStatusChangeException;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionStatusListener;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageHandler;
import org.kolaka.freecast.transport.PeerConnectionStatusMessage;
import org.kolaka.freecast.transport.PeerStatusMessage;

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
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("peerIdentifier", peerIdentifier);
		builder.append("status", status);
		builder.append("remoteStatus", remoteStatus);
		appendFields(builder);
		return builder.toString();
	}
	
	protected abstract void appendFields(ToStringBuilder builder);
	
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

	private MessageHandler handler;

	public void setMessageHandler(MessageHandler handler) {
		Validate.notNull(handler);
		this.handler = handler;
	}

	protected void processMessage(Message message) throws IOException {
		if (handler != null) {
			handler.messageReceived(message);
		}
	}

	private NodeStatusProvider statusProvider;
	
	protected NodeStatusProvider getNodeStatusProvider() {
		return statusProvider;
	}
	
	public void setNodeStatusProvider(NodeStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}
	
	protected void sendNodeStatus() {
		PeerStatus peerStatus = statusProvider.getNodeStatus().createPeerStatus();
		sendNodeStatus(peerStatus);
	}

	protected void sendNodeStatus(PeerStatus peerStatus) {
		if (getStatus().equals(PeerConnection.Status.CLOSED)) {
			LogFactory.getLog(getClass()).trace("ignore status sending on a closed connection");
			return;
		}
		
		LogFactory.getLog(getClass()).trace("send peer status");
		try {
			getWriter().write(new PeerStatusMessage(peerStatus));
		} catch (IOException e) {
			LogFactory.getLog(getClass()).error("can't send peer status", e);
		}
	}
	
	protected void sendConnectionStatus(PeerConnection.Status status) throws IOException {
		LogFactory.getLog(getClass()).debug("notify connection status: " + status);
		getWriter().write(new PeerConnectionStatusMessage(status));
	}

}
