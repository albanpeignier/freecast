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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageHandler;
import org.kolaka.freecast.transport.PeerConnectionStatusMessage;
import org.kolaka.freecast.transport.PeerStatusMessage;

public abstract class BasePeerConnection2 extends BasePeerConnection implements PeerConnection2 {

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
