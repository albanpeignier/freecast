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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.lang.SerializationUtils;
import org.kolaka.freecast.peer.PeerStatus;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerStatusMessage extends BaseMessage {
    private PeerStatus peerStatus;

    public PeerStatusMessage() {

    }

    public PeerStatusMessage(PeerStatus status) {
        this.peerStatus = status;
    }

    public boolean equals(Message other) {
        return other instanceof PeerStatusMessage
                && equals((PeerStatusMessage) other);
    }

    public boolean equals(PeerStatusMessage other) {
        return peerStatus.equals(other.peerStatus);
    }

    public MessageType getType() {
        return MessageType.PEERSTATUS;
    }

    public int hashCode() {
        return peerStatus.hashCode();
    }

    public void read(DataInputStream input) throws IOException {
        int length = input.readInt();
        byte bytes[] = new byte[length];
        input.readFully(bytes);
        peerStatus = (PeerStatus) SerializationUtils.deserialize(bytes);
    }

    public void write(DataOutputStream output) throws IOException {
        byte bytes[] = SerializationUtils.serialize(peerStatus);
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    public PeerStatus getPeerStatus() {
        return peerStatus;
    }
}