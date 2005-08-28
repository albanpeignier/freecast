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
import java.net.Socket;
import java.util.Iterator;

import javax.net.SocketFactory;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.peer.*;
import sun.rmi.runtime.Log;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class SocketPeerConnectionFactory extends PeerConnectionFactory {

    private SocketFactory socketFactory = SocketFactory.getDefault();

    public void setSocketFactory(SocketFactory socketFactory) {
        Validate.notNull(socketFactory, "No specified SocketFactory");
        this.socketFactory = socketFactory;
    }

    protected PeerConnection createImpl(Peer peer, PeerReference reference)
            throws PeerConnectionFactoryException {
        if (reference instanceof InetPeerReference) {
            return createImpl(peer, (InetPeerReference) reference);
        }

        if (reference instanceof MultiplePeerReference) {
            return createImpl(peer, (MultiplePeerReference) reference);
        }

        throw new PeerConnectionFactoryException("Unsupported PeerReference type: " + reference);
    }

    protected PeerConnection createImpl(Peer peer, MultiplePeerReference reference)
            throws PeerConnectionFactoryException
    {
        PeerConnectionFactoryException lastException = null;
        for (Iterator iterator = reference.references().iterator(); iterator.hasNext();) {
            PeerReference peerReference = (PeerReference) iterator.next();
            try {
                return createImpl(peer, peerReference);
            } catch (PeerConnectionFactoryException e) {
                LogFactory.getLog(getClass()).debug("Can't connect to " + peerReference + ", try next");
                lastException = e;
            }
        }

        if (lastException == null) {
            throw new PeerConnectionFactoryException("No reference provided by MultiplePeerReference " + reference);
        }
        throw lastException;
    }

    protected PeerConnection createImpl(Peer peer, InetPeerReference reference)
            throws PeerConnectionFactoryException
    {
        Socket socket;
        try {
            socket = socketFactory.createSocket();
            socket.setSoTimeout(10000);
			socket.setReceiveBufferSize(1024);
            socket.connect(reference.getSocketAddress(), 10000);

            return new SocketPeerConnection(PeerConnection.Type.SOURCE, socket);
        } catch (IOException e) {
            throw new PeerConnectionFactoryException(
                    "Can't create a PeerConnection to " + reference, e);
        }
    }

    private Socket connectSocket(InetPeerReference reference) throws IOException {
        Socket socket = socketFactory.createSocket();
        socket.setSoTimeout(10000);
        socket.setReceiveBufferSize(1024);
        socket.connect(reference.getSocketAddress(), 10000);
        return socket;
    }

}
