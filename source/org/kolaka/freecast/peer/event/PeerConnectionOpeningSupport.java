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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.kolaka.freecast.peer.PeerConnection;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnectionOpeningSupport {

    private final Set listeners = new HashSet();

    private final Set vetoableListeners = new HashSet();

    public void add(PeerConnectionOpeningListener listener) {
        listeners.add(listener);
    }

    public void remove(PeerConnectionOpeningListener listener) {
        listeners.remove(listener);
    }

    public void add(VetoablePeerConnectionOpeningListener listener) {
        vetoableListeners.add(listener);
    }

    public void remove(VetoablePeerConnectionOpeningListener listener) {
        vetoableListeners.remove(listener);
    }

    public void fireConnectionOpening(PeerConnection connection) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            PeerConnectionOpeningListener listener = (PeerConnectionOpeningListener) iter
                    .next();
            // TODO support other methods when needed
            listener.connectionOpening(connection);
        }
    }

    public void fireVetoableConnectionOpening(PeerConnection connection)
            throws VetoPeerConnectionOpeningException {
        for (Iterator iter = vetoableListeners.iterator(); iter.hasNext();) {
            VetoablePeerConnectionOpeningListener listener = (VetoablePeerConnectionOpeningListener) iter
                    .next();
            // TODO support other methods when needed
            listener.vetoableConnectionOpening(connection);
        }
    }

}