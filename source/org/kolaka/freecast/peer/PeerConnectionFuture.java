/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2006 Alban Peignier
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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.peer.PeerConnection.Status;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;

public class PeerConnectionFuture {

	private final PeerConnection connection;

	public PeerConnectionFuture(final PeerConnection connection) {
		Validate.notNull(connection);
		this.connection = connection;
	}
	
	public void wait(final PeerConnection.Status status, long timeout) throws PeerConnectionFactoryException {
		final Object lock = new Object();
		
		PeerConnectionStatusListener listener = new PeerConnectionStatusListener() {
			public void peerConnectionStatusChanged(PeerConnectionStatusEvent event) {
				Status newStatus = event.getStatus();
				if (newStatus.equals(status) || newStatus.equals(PeerConnection.Status.CLOSED)) {
					synchronized (lock) {
						lock.notifyAll();
					}
				}
			}
		};
		
		connection.add(listener);
		
		synchronized (lock) {
			try {
				lock.wait(timeout);
			} catch (InterruptedException e) {
				LogFactory.getLog(getClass()).error("Can't wait lock", e);
			}
		}
		
		connection.remove(listener);
		
		if (!connection.getStatus().equals(status)) {
			throw new PeerConnectionFactoryException("Timeout while waiting " + status + " on " + connection);
		}
	}
	
}
