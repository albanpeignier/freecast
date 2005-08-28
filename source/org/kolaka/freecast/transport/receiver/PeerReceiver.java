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

import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.packet.signer.DummyPacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidatorException;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.pipe.Producer;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Controlables;
import org.kolaka.freecast.service.LoopService;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.PacketMessage;

/**
 * 
 * @navassoc - - - org.kolaka.freecast.peer.PeerConnection
 * @navassoc - - - org.kolaka.freecast.transport.MessageReader
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerReceiver extends LoopService implements Receiver {

    private final PeerConnection connection;

    private Producer producer;

    public void setProducer(Producer producer) {
        this.producer = producer;
    }
    
    private PacketValidator packetValidator = new DummyPacketValidator();
    
    public void setPacketValidator(PacketValidator packetValidator) {
        Validate.notNull(packetValidator);
        this.packetValidator = packetValidator;
    }

    protected Loop createLoop() {
        return new Loop() {
            
            public long loop() {
                NDC.push(connection.toString());
                try {
                    readMessage();
                } finally {
                    NDC.pop();
                }
                return DefaultTimer.nodelay();
            }
            
        };
    }

    private void readMessage() {
        Message message;

        try {
            message = connection.getReader().read();
        } catch (EOFException e) {
            LogFactory.getLog(getClass()).debug(
                    "end of stream for " + connection, e);
            stopQuietly();
            return;
        } catch (IOException e) {
            stopOnError("can't read next message from " + connection, e);
            return;
        }

        if (message instanceof PacketMessage) {
            Packet packet = ((PacketMessage) message).getPacket();
            if (validatePacket(packet)) {
                producer.push(packet);    
            } else {
                LogFactory.getLog(getClass()).error("unvalidate packet: " + packet);    
            }
        } else {
            LogFactory.getLog(getClass()).debug("ignore " + message);
        }
    }
    
    private boolean validatePacket(Packet packet) {
        try {
            return packetValidator.validate(packet);
        } catch (PacketValidatorException e) {
            LogFactory.getLog(getClass()).error("can't validate the packet " + packet,e);
            return false;
        }
    }

    private void stopOnError(String message, Throwable cause) {
        LogFactory.getLog(getClass()).warn("stop on error, " + message, cause);
        stopQuietly();
    }

    public void stop() throws ControlException {
		producer.close();

        super.stop();

        if (!connection.getStatus().equals(PeerConnection.Status.CLOSED)) {
            connection.close();
        }
    }

    private final PeerConnectionStatusListener listener = new PeerConnectionStatusListener() {
        public void peerConnectionStatusChanged(PeerConnectionStatusEvent event) {
            if (event.getStatus().equals(PeerConnection.Status.CLOSED)) {
                Controlables.stopQuietly(PeerReceiver.this);
            }
        }
    };

    public PeerReceiver(final PeerConnection connection) {
        Validate.isTrue(
                connection.getType().equals(PeerConnection.Type.SOURCE),
                "connection type must be " + PeerConnection.Type.SOURCE,
                connection);

        this.connection = connection;
        connection.add(listener);
    }

    public boolean isStopped() {
        return super.isStopped();
    }

}