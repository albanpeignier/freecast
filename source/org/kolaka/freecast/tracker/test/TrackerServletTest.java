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

package org.kolaka.freecast.tracker.test;

import java.net.InetSocketAddress;

import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.test.BaseTestCase;
import org.kolaka.freecast.tracker.HttpMultiTrackerLocator;
import org.kolaka.freecast.tracker.HttpTracker;
import org.kolaka.freecast.tracker.HttpTrackerLocator;
import org.kolaka.freecast.tracker.NetworkIdentifier;
import org.kolaka.freecast.tracker.Tracker;
import org.kolaka.freecast.tracker.TrackerException;
import org.kolaka.freecast.tracker.TrackerLocator;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class TrackerServletTest extends BaseTestCase {

	private InetSocketAddress address;

  protected void setUp() throws Exception {
    super.setUp();
    address = new InetSocketAddress(50000 + (int) (Math
        .random() * 1000));
  }
  
  public void testSingle() throws Exception {
    testBindConnect(false, new HttpTrackerLocator(address));
  }

  public void testMulti() throws Exception {
    testBindConnect(true, new HttpMultiTrackerLocator(address, NetworkIdentifier.getRandomInstance()));
  }

  private void testBindConnect(boolean multiTracker, TrackerLocator locator) throws Exception {
    HttpTracker tracker = new HttpTracker();

    tracker.setMultiTracker(multiTracker);
		tracker.setListenAddress(address);
    
		tracker.start();
    
    try {
  		Tracker remoteTracker = locator.resolve();
      
  		InetPeerReference nodeReference = InetPeerReference.getInstance(new InetSocketAddress(4000));
      testRemoteTracker(remoteTracker, nodeReference);
      
      testRemoteTracker(remoteTracker, null);
    } finally {
      tracker.stop();
    }
	}

  /**
   * @param remoteTracker
   * @param nodeReference
   * @throws TrackerException
   */
  private void testRemoteTracker(Tracker remoteTracker, InetPeerReference nodeReference) throws TrackerException {
    NodeIdentifier nodeIdentifier = remoteTracker.register(nodeReference);
    remoteTracker.getPeerReferences(nodeIdentifier);
    remoteTracker.unregister(nodeIdentifier);
  }
  

}