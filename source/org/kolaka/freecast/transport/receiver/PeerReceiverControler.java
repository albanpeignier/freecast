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

package org.kolaka.freecast.transport.receiver;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.packet.signer.DigestPacketChecksummer;
import org.kolaka.freecast.packet.signer.DigestPacketValidator;
import org.kolaka.freecast.packet.signer.DummyPacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidatorUser;
import org.kolaka.freecast.peer.NoPeerAvailableException;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnectionFactoryException;
import org.kolaka.freecast.peer.PeerControler;
import org.kolaka.freecast.pipe.Pipe;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Controlables;
import org.kolaka.freecast.service.Service;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerReceiverControler implements ReceiverControler, TimerUser, PacketValidatorUser {

    private Pipe pipe;

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    private PeerControler peerControler;

    public void setPeerControler(PeerControler peerControler) {
        this.peerControler = peerControler;
    }

    private final Task startReceiverTask = new Task() {
        public void run() {
            long retryDelay = -1;

            try {
                createReceiver();
            } catch (NoPeerAvailableException e) {
                LogFactory.getLog(getClass()).debug("no peer available");
                retryDelay = DefaultTimer.seconds(5);
            } catch (PeerConnectionFactoryException e) {
                LogFactory.getLog(getClass()).debug(
                        "error while establishing connection", e);
                retryDelay = DefaultTimer.seconds(2);
            } catch (Throwable e) {
                LogFactory.getLog(getClass()).error("receiver creation failed",
                        e);
                retryDelay = DefaultTimer.seconds(3);
            }

            if (retryDelay > -1) {
                startReceiver(retryDelay);
            }
        }
    };

    private Service.Listener listener = new Service.Adapter() {
        public void serviceStopped(Service service) {
            if (ObjectUtils.equals(receiver, service)) {
                disposeReceiver();
            }

            service.remove(this);
        }
    };

    private PacketValidator packetValidator = new DummyPacketValidator();
    
    public void setPacketValidator(PacketValidator packetValidator) {
        Validate.notNull(packetValidator);
        this.packetValidator = packetValidator;
    }

    private PeerReceiver receiver;

    
    /**
     * @todo review the exception handling
     * 
     * @throws NoPeerAvailableException
     * @throws PeerConnectionFactoryException
     * @throws ControlException
     */
    private void createReceiver() throws NoPeerAvailableException,
            PeerConnectionFactoryException, ControlException {
        if (receiver != null) {
            throw new IllegalStateException("Receiver already exists: "
                    + receiver);
        }

        Peer peer = peerControler.getBestPeer();

        LogFactory.getLog(getClass()).debug(
                "try to open a connection with " + peer);
        PeerConnection peerConnection = peer.connect();
        peerConnection.activate();

        LogFactory.getLog(getClass()).debug(
                "create a peer receiver for " + peerConnection);
        PeerReceiver receiver = new PeerReceiver(peerConnection);
        receiver.setPacketValidator(packetValidator);
        receiver.setProducer(pipe.createProducer());

        receiver.add(listener);

        LogFactory.getLog(getClass()).debug("start peer receiver " + receiver);
        Controlables.start(receiver);

        this.receiver = receiver;
    }

    private Timer timer = DefaultTimer.getInstance();

    public void setTimer(Timer timer) {
        Validate.notNull(timer, "No specified Timer");
        this.timer = timer;
    }

    private void startReceiver() {
        LogFactory.getLog(getClass()).debug("start a new receiver");
        timer.executeLater(startReceiverTask);
    }

    private void startReceiver(long delay) {
        LogFactory.getLog(getClass()).debug(
                "start a new receiver in " + delay + " ms");
        timer.executeAfterDelay(delay, startReceiverTask);
    }

    private void disposeReceiver() {
        LogFactory.getLog(getClass()).debug(
                "dispose current receiver " + receiver);
        receiver = null;
        if (!stopped) {
            startReceiver();
        }
    }

    public void start() throws ControlException {
        LogFactory.getLog(getClass()).debug("start");
        startReceiver();
    }

    private boolean stopped;

    public void stop() throws ControlException {
        stopped = true;
        if (receiver != null) {
            receiver.stop();
        }
    }

    public void dispose() throws ControlException {

    }

    public void init() throws ControlException {

    }

    public PeerReceiverControler(PeerControler peerControler) {
        this.peerControler = peerControler;
    }
}