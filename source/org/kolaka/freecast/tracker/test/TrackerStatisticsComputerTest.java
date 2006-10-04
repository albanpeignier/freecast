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

package org.kolaka.freecast.tracker.test;

import junit.framework.TestCase;

import org.kolaka.freecast.tracker.DefaultTrackerStatistics;
import org.kolaka.freecast.tracker.TrackerStatisticsComputer;

public class TrackerStatisticsComputerTest extends TestCase {

  private TrackerStatisticsComputer computer = new TrackerStatisticsComputer();
  
  public void testStatistics() {
    assertEquals(new DefaultTrackerStatistics(), computer.getStatistics());
    
    computer.nodeConnected(true);
    testStatistics(1, 1, true, 0);
    
    computer.nodeConnected(false);
    testStatistics(2, 1, true, 1);

    computer.nodeConnected(false);
    testStatistics(3, 1, true, 2);

    computer.nodeDisconnected(false);
    testStatistics(3, 1, true, 1);

    computer.nodeDisconnected(true);
    testStatistics(3, 1, false, 1);

    computer.nodeDisconnected(false);
    testStatistics(3, 1, false, 0);

    computer.nodeDisconnected(false);
    testStatistics(3, 1, false, 0);
  }

  private void testStatistics(int nodeConnections, int rootNodeConnections,
      boolean rootNodePresents, int listenerConnected) {
    DefaultTrackerStatistics statistics = 
      new DefaultTrackerStatistics(nodeConnections, rootNodeConnections, rootNodePresents, listenerConnected);
    assertEquals(statistics, computer.getStatistics());
  }

}
