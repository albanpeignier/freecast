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

package org.kolaka.freecast.tracker.statistics;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.tracker.NetworkIdentifier;

public class DefaultTrackerStatistics implements TrackerStatistics {

  private int nodeConnections;
  private int rootNodeConnections;
  
  private boolean rootNodePresents;
  
  private int listenerConnected;
  
  private NetworkIdentifier networkId;
  
  public DefaultTrackerStatistics() {
    
  }
  
  public DefaultTrackerStatistics(int nodeConnections, int rootNodeConnections,
      boolean rootNodePresents, int listenerConnected) {
    super();
    this.nodeConnections = nodeConnections;
    this.rootNodeConnections = rootNodeConnections;
    this.rootNodePresents = rootNodePresents;
    this.listenerConnected = listenerConnected;
  }

  public int getNodeConnections() {
    return nodeConnections;
  }

  public int getRootNodeConnections() {
    return rootNodeConnections;
  }

  public boolean isRootNodePresents() {
    return rootNodePresents;
  }

  public int getListenerConnected() {
    return listenerConnected;
  }

  public void setNodeConnections(int nodeConnections) {
    this.nodeConnections = nodeConnections;
  }

  public void setRootNodeConnections(int rootNodeConnections) {
    this.rootNodeConnections = rootNodeConnections;
  }

  public void setRootNodePresents(boolean rootNodePresents) {
    this.rootNodePresents = rootNodePresents;
  }

  public void setListenerConnected(int listenerConnected) {
    this.listenerConnected = listenerConnected;
  }
  
  public NetworkIdentifier getNetworkId() {
    return networkId;
  }
  
  public void setNetworkId(NetworkIdentifier networkId) {
    this.networkId = networkId;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
}
