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

package org.kolaka.freecast.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.kolaka.freecast.peer.PeerConnection;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnectionStatusMessage extends BaseMessage {
	
	private PeerConnection.Status connectionStatus;

	public PeerConnectionStatusMessage() {

	}

	public PeerConnectionStatusMessage(PeerConnection.Status status) {
		this.connectionStatus = status;
	}

	public boolean equals(Message other) {
		return other instanceof PeerConnectionStatusMessage
				&& equals((PeerConnectionStatusMessage) other);
	}

	public boolean equals(PeerConnectionStatusMessage other) {
		return connectionStatus.equals(other.connectionStatus);
	}

	public MessageType getType() {
		return MessageType.CONNECTIONSTATUS;
	}

	public int hashCode() {
		return connectionStatus.hashCode();
	}

	protected void readImpl(DataInputStream input) throws IOException {
		int value = input.readInt();
		connectionStatus = PeerConnection.Status.getStatus(value);
	}

	protected void writeImpl(DataOutputStream output) throws IOException {
		output.writeInt(connectionStatus.getValue());
	}

	public PeerConnection.Status getStatus() {
		return connectionStatus;
	}
	
}