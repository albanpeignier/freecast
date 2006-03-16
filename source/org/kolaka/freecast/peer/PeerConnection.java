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

package org.kolaka.freecast.peer;

import java.io.IOException;

import org.apache.commons.lang.enums.ValuedEnum;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.PeerStatusListener;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionStatusListener;
import org.kolaka.freecast.transport.MessageWriter;

/**
 * @has - - 1 org.kolaka.freecast.transport.MessageReader
 * @has - - 1 org.kolaka.freecast.transport.MessageWriter
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public interface PeerConnection {

	NodeIdentifier getPeerIdentifier();
	
	Status getStatus();

	MessageWriter getWriter();

	void close() throws IOException;

	void add(PeerConnectionStatusListener listener);
	
	void remove(PeerConnectionStatusListener listener);
	
	void add(VetoablePeerConnectionStatusListener listener);
	
	void remove(VetoablePeerConnectionStatusListener listener);

	void add(PeerStatusListener listener);
	
	void remove(PeerStatusListener listener);
	
	void setNodeStatusProvider(NodeStatusProvider statusProvider);
	
	PeerStatus getRemoteStatus();

	public static class Status extends ValuedEnum {

		private static final long serialVersionUID = 3257846593196603700L;

		public static final Status INITIAL = new Status("initial", 20);

		public static final Status OPENING = new Status("opening", 0);

		public static final Status OPENED = new Status("opened", 1);

		public static final Status ACTIVATED = new Status("activated", 5);

		public static final Status CLOSING = new Status("closing", 10);

		public static final Status CLOSED = new Status("closed", 11);

		private Status(String name, int value) {
			super(name, value);
		}

		public static Status getStatus(int value) {
			Status status = (Status) getEnum(Status.class, value);
			if (status == null) {
				throw new IllegalArgumentException("Unknown Status value: "
						+ value);
			}
			return status;
		}

	}

}