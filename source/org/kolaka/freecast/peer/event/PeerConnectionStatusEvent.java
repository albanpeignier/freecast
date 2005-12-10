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

package org.kolaka.freecast.peer.event;

import java.util.EventObject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.peer.PeerConnection;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnectionStatusEvent extends EventObject {

	private static final long serialVersionUID = 3257008752401396274L;

	private PeerConnection.Status status;

	public PeerConnectionStatusEvent(PeerConnection connection,
			PeerConnection.Status status) {
		super(connection);
		this.status = status;
	}

	public PeerConnection getConnection() {
		return (PeerConnection) getSource();
	}

	public PeerConnection.Status getStatus() {
		return status;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}