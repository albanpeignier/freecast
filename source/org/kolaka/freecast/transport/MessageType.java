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

import org.apache.commons.lang.enums.ValuedEnum;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class MessageType extends ValuedEnum {

	private static final long serialVersionUID = 3834590989301069619L;

	public static final int ID_PACKET = 1;

	public static final MessageType PACKET = new MessageType("packet",
			ID_PACKET, PacketMessage.class);

	public static final int ID_PEERSTATUS = 3;

	public static final MessageType PEERSTATUS = new MessageType("peerStatus",
			ID_PEERSTATUS, PeerStatusMessage.class);

	public static final int ID_CONNECTIONSTATUS = 4;

	public static final MessageType CONNECTIONSTATUS = new MessageType(
			"connectionStatus", ID_CONNECTIONSTATUS,
			PeerConnectionStatusMessage.class);

	private final Class messageClass;

	public MessageType(String value, int identifier, Class messageClass) {
		super(value, identifier);
		this.messageClass = messageClass;
	}

	public Class getMessageClass() {
		return messageClass;
	}

	public static MessageType get(int identifier) {
		return (MessageType) getEnum(MessageType.class, identifier);
	}

}