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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.peer.PeerConnection;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnectionStatusSupport {

	private final PeerConnection source;

	public PeerConnectionStatusSupport(PeerConnection source) {
		this.source = source;
	}

	private final Set listeners = new HashSet();
	private final Set vetoListeners = new HashSet();

	public void add(PeerConnectionStatusListener listener) {
		listeners.add(listener);
	}

	public void remove(PeerConnectionStatusListener listener) {
		listeners.remove(listener);
	}

	public void add(VetoablePeerConnectionStatusListener listener) {
		vetoListeners.add(listener);
	}

	public void remove(VetoablePeerConnectionStatusListener listener) {
		vetoListeners.remove(listener);
	}
	
	public void checkVetoStatus(PeerConnection.Status status) throws VetoPeerConnectionStatusChangeException {
		fireVetoable(new PeerConnectionStatusEvent(source, status));
	}

	public void fireStatus(PeerConnection.Status status) {
		fire(new PeerConnectionStatusEvent(source, status));
	}

	public void fire(PeerConnectionStatusEvent event) {
		LogFactory.getLog(getClass()).trace("fire " + event);
		for (Iterator iter = new ArrayList(listeners).iterator(); iter
				.hasNext();) {
			PeerConnectionStatusListener listener = (PeerConnectionStatusListener) iter
					.next();
			listener.peerConnectionStatusChanged(event);
		}
	}

	public void fireVetoable(PeerConnectionStatusEvent event) throws VetoPeerConnectionStatusChangeException {
		LogFactory.getLog(getClass()).trace("fire " + event);
		for (Iterator iter = new ArrayList(vetoListeners).iterator(); iter
				.hasNext();) {
			VetoablePeerConnectionStatusListener listener = (VetoablePeerConnectionStatusListener) iter
					.next();
			listener.vetoablePeerConnectionStatusChange(event);
		}
	}
	
	public void clear() {
		listeners.clear();
		vetoListeners.clear();
	}

}