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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;

public class AcknowledgmentMessage extends BaseMessage {
	
	public AcknowledgmentMessage() {
		
	}

	private Set ackIdentifiers = new TreeSet(); 
	
	public void add(AcknowledgableMessage message) {
		Validate.isTrue(message.isImportant());
		ackIdentifiers.add(new Integer(message.getAcknowledgmentIdentifier()));
	}
	
	public boolean isEmpty() {
		return ackIdentifiers.isEmpty();
	}
	
	public Iterator identifiers() {
		return ackIdentifiers.iterator();
	}
	
	protected void writeImpl(DataOutputStream output) throws IOException {
		output.writeByte(ackIdentifiers.size());
		for (Iterator iter = ackIdentifiers.iterator(); iter.hasNext();) {
			Integer ackIdentifier = (Integer) iter.next();
			output.writeInt(ackIdentifier.intValue());
		}
	}

	protected void readImpl(DataInputStream input) throws IOException {
		int ackIdentifierCount = input.readByte();
		for (int i=0; i < ackIdentifierCount; i++) {
			ackIdentifiers.add(new Integer(input.readInt()));
		}
	}

	public boolean equals(Message other) {
		return other instanceof AcknowledgmentMessage && equals((AcknowledgmentMessage) other);
	}
	
	public boolean equals(AcknowledgmentMessage other) {
		return other != null && ackIdentifiers.equals(other.ackIdentifiers);
	}

	public MessageType getType() {
		return MessageType.ACKNOWLEDGMENT;
	}

}
