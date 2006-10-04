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

package org.kolaka.freecast.tracker;

import java.util.Set;

import javax.servlet.ServletException;

import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.peer.PeerReference;

public class HttpMultiTrackerConnector extends HttpTrackerConnector implements MultiTracker {

  private static final long serialVersionUID = 1568160755433034061L;
  private MultiTracker tracker;
  
  public void init() throws ServletException {
    super.init();
    tracker = (MultiTracker) getTracker();
  }

  public Set getPeerReferences(NetworkIdentifier networkIdentifier, NodeIdentifier identifier)
      throws TrackerException {
    return tracker.getPeerReferences(networkIdentifier, identifier);
  }

  public NodeIdentifier register(NetworkIdentifier networkIdentifier, PeerReference reference)
      throws TrackerException {
    return tracker.register(networkIdentifier, reference);
  }

  public void unregister(NetworkIdentifier networkIdentifier, NodeIdentifier identifier) throws TrackerException {
    tracker.unregister(networkIdentifier, identifier);
  }

  public void refresh(NetworkIdentifier networkIdentifier, NodeStatus status) throws TrackerException {
    tracker.refresh(networkIdentifier, status);
  }

}
