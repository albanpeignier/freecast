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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class StreamMessageReader implements MessageReader {

	private final DataInputStream input;

	public StreamMessageReader(final InputStream input) {
		this.input = new DataInputStream(input);
	}

	private byte capturePatternBuffer[] = new byte[Message.CAPTURE_PATTERN.length];

	public Message read() throws IOException {
		input.readFully(capturePatternBuffer);
		if (!Arrays.equals(Message.CAPTURE_PATTERN, capturePatternBuffer)) {
			throw new IOException("Invalid capture pattern");
		}

		int typeId = input.readUnsignedByte();
		MessageType type = MessageType.get(typeId);
		if (type == null) {
			throw new IOException("Invalid type identifier " + typeId);
		}

		Message message = createMessage(type);
		message.read(input);
		return message;
	}

	protected Message createMessage(MessageType type) throws IOException {
		Class messageClass = type.getMessageClass();
		try {
			return (Message) messageClass.newInstance();
		} catch (Exception e) {
			String msg = "Can't create a new Message (" + type + ","
					+ messageClass.getName() + ")";
			IOException exception = new IOException(msg);
			exception.initCause(e);
			throw exception;
		}
	}

}