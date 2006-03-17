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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.packet.signer.DummyPacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidatorException;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnections;
import org.kolaka.freecast.peer.PeerReceivingConnection;
import org.kolaka.freecast.peer.event.PeerConnectionStatusAdapter;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.pipe.Producer;
import org.kolaka.freecast.service.BaseService;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Controlables;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageHandler;
import org.kolaka.freecast.transport.PacketMessage;

/**
 * 
 * @navassoc - - - org.kolaka.freecast.peer.PeerConnection
 * @navassoc - - - org.kolaka.freecast.transport.MessageReader
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerReceiver extends BaseService implements Receiver {

	private final PeerReceivingConnection connection;

	private Producer producer;

	public void setProducer(Producer producer) {
		this.producer = producer;
		connection.setMessageHandler(messageHandler);
	}

	private PacketValidator packetValidator = new DummyPacketValidator();

	public void setPacketValidator(PacketValidator packetValidator) {
		Validate.notNull(packetValidator);
		this.packetValidator = packetValidator;
	}

	private boolean validatePacket(Packet packet) {
		try {
			return packetValidator.validate(packet);
		} catch (PacketValidatorException e) {
			LogFactory.getLog(getClass()).error(
					"can't validate the packet " + packet, e);
			return false;
		}
	}

	public void stop() throws ControlException {
		producer.close();

		super.stop();

		if (!connection.getStatus().equals(PeerConnection.Status.CLOSED)) {
			PeerConnections.closeQuietly(connection);
		}
	}

	private final PeerConnectionStatusListener listener = new PeerConnectionStatusAdapter() {
		protected void connectionClosed(PeerConnection connection) {
			Controlables.stopQuietly(PeerReceiver.this);
		}
	};
	
	private final MessageHandler messageHandler = new MessageHandler() {
		public void messageReceived(Message message) {
			if (message instanceof PacketMessage) {
				Packet packet = ((PacketMessage) message).getPacket();
				if (validatePacket(packet)) {
					producer.push(packet);
				} else {
					LogFactory.getLog(getClass()).error(
							"unvalidate packet: " + packet);
				}
			} else {
				LogFactory.getLog(getClass()).trace("ignore " + message);
			}
		}
	};

	public PeerReceiver(final PeerReceivingConnection connection) {
		this.connection = connection;
		connection.add(listener);
	}
	
	private static final ReceiverConfiguration CONFIGURATION = new PeerReceiverConfiguration();
	
	public ReceiverConfiguration getReceiverConfiguration() {
		return CONFIGURATION;
	}

}