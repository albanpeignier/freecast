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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;

public class DefaultMultiTracker implements MultiTracker, TimerUser, ClientInfoProviderUser, MultiTrackerStatisticsProvider {

  private Map trackers = new TreeMap();

  private ClientInfoProvider clientInfoProvider;

  public DefaultMultiTracker() {
    Runnable purgeRunnable = new Runnable() {
      public void run() {
        purge();
      }
    };
    timer.executePeriodically(DefaultTimer.minutes(1), purgeRunnable , false);
  }
  
  public TrackerStatistics getStatistics(NetworkIdentifier identifier) {
    Tracker tracker = (Tracker) trackers.get(identifier);
    if (tracker == null) {
      return new DefaultTrackerStatistics();
    }
    return ((TrackerStatisticsProvider) tracker).getStatistics();
  }

  public void setClientInfoProvider(ClientInfoProvider clientInfoProvider) {
    this.clientInfoProvider = clientInfoProvider;
  }
  
  private ClientInfoProvider getClientInfoProvider() {
    if (clientInfoProvider == null) {
      throw new IllegalStateException("No specified ClientInfoProvider");
    }
    return clientInfoProvider;
  }

  private synchronized Tracker getTracker(NetworkIdentifier identifier) {
    Tracker tracker = (Tracker) trackers.get(identifier);
    if (tracker == null) {
      LogFactory.getLog(getClass()).info("create tracker for network " + identifier);
      tracker = createTracker(getClientInfoProvider());
      trackers.put(identifier, tracker);
    }
    return tracker;
  }
  
  protected Tracker createTracker(ClientInfoProvider clientInfoProvider) {
    DefaultTracker tracker = new DefaultTracker();
    tracker.setClientInfoProvider(clientInfoProvider);
    return new TimedTracker(tracker);
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
  
  private Timer timer = DefaultTimer.getInstance();
  
  public void setTimer(Timer timer) {
    Validate.notNull(timer);
    this.timer = timer;
  }
    
  private synchronized void purge() {
    Date olderLastRequest = new Date(System.currentTimeMillis() - DefaultTimer.minutes(5));
    LogFactory.getLog(getClass()).debug("purge trackers unused since " + olderLastRequest);
    for (Iterator iter = trackers.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      NetworkIdentifier networkId = (NetworkIdentifier) entry.getKey();
      TimedTracker tracker = (TimedTracker) entry.getValue();
      
      if (tracker.getLastRequest().before(olderLastRequest)) {
        LogFactory.getLog(getClass()).info("purge tracker " + networkId);
        iter.remove();
      }
    }
    LogFactory.getLog(getClass()).debug(trackers.size() + " trackers kept");
  }
  
  static class TimedTracker implements Tracker, TrackerStatisticsProvider {
    
    private final Tracker delegate;
    private Date lastRequest = new Date();
    
    public TimedTracker(final Tracker delegate) {
      Validate.notNull(delegate);
      this.delegate = delegate;
    }
    
    public TrackerStatistics getStatistics() {
      return ((TrackerStatisticsProvider) delegate).getStatistics();
    }

    public Date getLastRequest() {
      return lastRequest;
    }

    public Set getPeerReferences(NodeIdentifier node) throws TrackerException {
      updateLastRequest();
      return delegate.getPeerReferences(node);
    }

    public void refresh(NodeStatus status) throws TrackerException {
      updateLastRequest();
      delegate.refresh(status);
    }

    public NodeIdentifier register(PeerReference reference) throws TrackerException {
      updateLastRequest();
      return delegate.register(reference);
    }

    public void unregister(NodeIdentifier identifier) throws TrackerException {
      updateLastRequest();
      delegate.unregister(identifier);
    }

    private void updateLastRequest() {
      lastRequest = new Date();
    }
    
  }

}
