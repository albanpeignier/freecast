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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.peer.PeerReference;

public class MultiTrackerAdapter implements Tracker {

  private NetworkIdentifier networkId;

  private MultiTracker tracker;

  public MultiTrackerAdapter(NetworkIdentifier networkId, MultiTracker tracker) {
    Validate.notNull(networkId);
    Validate.notNull(tracker);

    this.networkId = networkId;
    this.tracker = tracker;
  }
  
  private void logInvocation(String description) {
    LogFactory.getLog(getClass()).debug(description + " on network " + networkId);
  }

  public Set getPeerReferences(NodeIdentifier node) throws TrackerException {
    logInvocation("getPeerStatus for " + node);
    return tracker.getPeerReferences(networkId, node);
  }

  public void refresh(NodeStatus status) throws TrackerException {
    logInvocation("refresh " + status);
    tracker.refresh(networkId, status);
  }

  public NodeIdentifier register(PeerReference reference)
      throws TrackerException {
    logInvocation("register " + reference);
    return tracker.register(networkId, reference);
  }

  public void unregister(NodeIdentifier identifier) throws TrackerException {
    logInvocation("unregister " + identifier);
    tracker.unregister(this.networkId, identifier);
  }

}
