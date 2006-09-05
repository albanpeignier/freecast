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

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.tracker.DefaultTracker.ClientInfoProvider;

public class DefaultMultiTracker {

  private Map trackers = new TreeMap();

  private final ClientInfoProvider clientInfoProvider;

  public DefaultMultiTracker(final ClientInfoProvider clientInfoProvider) {
    this.clientInfoProvider = clientInfoProvider;
  }

  private Tracker getTracker(NetworkIdentifier identifier) {
    Tracker tracker = (Tracker) trackers.get(identifier);
    if (tracker == null) {
      LogFactory.getLog(getClass()).info("create tracker for network " + identifier);
      tracker = createTracker(clientInfoProvider);
      trackers.put(identifier, tracker);
    }
    return tracker;
  }
  
  protected Tracker createTracker(ClientInfoProvider clientInfoProvider) {
    return new DefaultTracker(clientInfoProvider);
  }

  public NodeIdentifier register(NetworkIdentifier network,
      PeerReference reference) throws TrackerException {
    return getTracker(network).register(reference);
  }

  public void unregister(NetworkIdentifier network, NodeIdentifier identifier)
      throws TrackerException {
    getTracker(network).unregister(identifier);
  }

  public void refresh(NetworkIdentifier network, NodeStatus status)
      throws TrackerException {
    getTracker(network).refresh(status);
  }

  public Set getPeerReferences(NetworkIdentifier network, NodeIdentifier node)
      throws TrackerException {
    return getTracker(network).getPeerReferences(node);
  }

}
