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

import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnectionException;

import java.io.IOException;
import java.net.Socket;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class SocketPeerConnection extends PeerConnection {

    private Socket socket;

    private MessageWriter writer;

    private MessageReader reader;

    public SocketPeerConnection(Type type, Socket socket) throws IOException {
        super(type);
        this.socket = socket;

        this.writer = new StreamMessageWriter(socket.getOutputStream());
        this.reader = new StreamMessageReader(socket.getInputStream());
    }

    protected MessageReader createReader() {
        return reader;
    }

    protected MessageWriter createWriter() {
        return writer;
    }

    protected void disposeImpl() throws PeerConnectionException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new PeerConnectionException("Can't dispose the socket", e);
        }
    }

    public String toString() {
        return "SocketPeerConnection[" + socket.getInetAddress() + "]";
    }

}