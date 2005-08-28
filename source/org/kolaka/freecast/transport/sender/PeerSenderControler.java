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

package org.kolaka.freecast.transport.sender;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerControler;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.VetoPeerConnectionOpeningException;
import org.kolaka.freecast.pipe.Pipe;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Controlables;
import org.kolaka.freecast.service.Service;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerSenderControler implements SenderControler {

    private PeerControler peerControler;

    private Pipe pipe;

    public void setPipe(Pipe pipe) {
        Validate.notNull(pipe, "No specified Pipe");
        this.pipe = pipe;
    }

    public void init() throws ControlException {
        // deprecated
    }

    public void dispose() throws ControlException {
        // deprecated
    }

    private final PeerConnectionStatusListener connectionListener = new PeerConnectionStatusListener() {

        public void peerConnectionStatusChanged(PeerConnectionStatusEvent event) {
            if (event.getStatus().equals(PeerConnection.Status.ACTIVATED)
                    && event.getConnection().getType().equals(
                            PeerConnection.Type.RELAY)) {
                createSender(event.getConnection());
            }
        }

    };

    private Service.Listener senderListener = new Service.Adapter() {
        public void serviceStopped(Service service) {
            PeerSender sender = (PeerSender) service;

            LogFactory.getLog(getClass()).debug("sender stopped: " + sender);
            senders.remove(sender);
        }
    };

    private Set senders = new HashSet();

    /**
     * @todo find a better Exception
     * @param connection
     * @throws VetoPeerConnectionOpeningException
     */
    private void createSender(PeerConnection connection) {
        LogFactory.getLog(getClass()).debug("create sender for " + connection);
        PeerSender sender = new PeerSender(connection);

        String consumerName = "sender-"
                + connection.getLastPeerStatus().getIdentifier().toString();
        sender.setConsumer(pipe.createConsumer(consumerName));

        sender.add(senderListener);
        try {
            Controlables.start(sender);
        } catch (ControlException e) {
            String msg = "Can't start the peer sender " + sender
                    + ", stop connection";
            LogFactory.getLog(getClass()).error(msg, e);

            connection.close();

            return;
        }

        senders.add(sender);
    }

    public void start() throws ControlException {
        if (peerControler == null) {
            throw new IllegalStateException("No defined PeerConnectionSource");
        }

        peerControler.add(connectionListener);
    }

    public void stop() throws ControlException {
        peerControler.remove(connectionListener);
    }

    public void setPeerControler(PeerControler peerControler) {
        this.peerControler = peerControler;
    }

}