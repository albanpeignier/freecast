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

package org.kolaka.freecast.transport;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoSession;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerSendingConnection;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.peer.event.VetoPeerConnectionStatusChangeException;
import org.kolaka.freecast.timer.Task;

public class MinaPeerSendingConnection extends BaseMinaPeerConnection implements PeerSendingConnection {
	
	public MinaPeerSendingConnection(IoSession session) {
		open(session);
	}
	
	private Date lastReceivedMessage = new Date();
	
	protected void processMessage(Message message) throws IOException {
		lastReceivedMessage = new Date();
		
		if (message instanceof PeerStatusMessage) {
			// TODO move up to BaseMinaPeerConnection
			PeerStatus remoteStatus = ((PeerStatusMessage) message).getPeerStatus();
			firePeerStatus(remoteStatus);

			if (getStatus().equals(PeerConnection.Status.OPENING)) {
				try {
					changeStatus(PeerConnection.Status.OPENED);
				} catch (VetoPeerConnectionStatusChangeException e) {
					LogFactory.getLog(getClass()).debug("can't open connection with " + remoteStatus.getIdentifier(), e);
					closeImpl();
					return;
				}
			}
			
			sendNodeStatus();
		} else if (message instanceof PeerConnectionStatusMessage) {
			PeerConnection.Status status = ((PeerConnectionStatusMessage) message).getStatus();
			LogFactory.getLog(getClass()).debug("connection status accepted from remote: " + status);
			try {
				changeStatus(status);
			} catch (VetoPeerConnectionStatusChangeException e) {
				LogFactory.getLog(getClass()).debug("can't change status to " + status, e);
			}
			
			if (!status.equals(PeerConnection.Status.CLOSED)) {
				LogFactory.getLog(getClass()).debug("reply effective connection status: " + getStatus());
				sendConnectionStatus(getStatus());
			}
		}

		super.processMessage(message);
	}
	
	protected Task createAliveTask() {
		return new Task() {
			public void run() {
				long lastMessageAge = System.currentTimeMillis() - lastReceivedMessage.getTime();
				LogFactory.getLog(getClass()).debug("check last message age: " + lastMessageAge);
				if (lastMessageAge > 2 * PING_DELAY) {
					LogFactory.getLog(getClass()).debug("lost connection: " + this);
					closeImpl();
				}
			}
		};
	}

}
