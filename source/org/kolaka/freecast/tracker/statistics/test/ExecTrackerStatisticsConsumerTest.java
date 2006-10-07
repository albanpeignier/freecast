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

package org.kolaka.freecast.tracker.statistics.test;

import java.io.File;
import java.util.Date;

import org.apache.commons.lang.SystemUtils;
import org.easymock.MockControl;
import org.kolaka.freecast.test.BaseTestCase;
import org.kolaka.freecast.tracker.NetworkIdentifier;
import org.kolaka.freecast.tracker.statistics.DefaultTrackerStatistics;
import org.kolaka.freecast.tracker.statistics.ExecTrackerStatisticsConsumer;
import org.kolaka.freecast.tracker.statistics.ExecTrackerStatisticsConsumer.Executor;

public class ExecTrackerStatisticsConsumerTest extends BaseTestCase {

  public void testProcess() {
    String command = "command";
    File workingDir = new File(SystemUtils.USER_DIR);

    ExecTrackerStatisticsConsumer consumer = new ExecTrackerStatisticsConsumer();
    consumer.setCommand(command);
    consumer.setWorkingDir(workingDir);
    
    MockControl executorControl = MockControl.createControl(Executor.class);
    Executor executor = (Executor) executorControl.getMock();
    
    executor.execute(command + " 0 0 1 2 3 true", workingDir);
    
    executorControl.replay();
    
    consumer.setExecutor(executor);
    
    DefaultTrackerStatistics stats = new DefaultTrackerStatistics(1, 2, true, 3);
    stats.setNetworkId(NetworkIdentifier.getInstance("0"));
    consumer.process(new Date(0), stats);
    
    executorControl.verify();
  }

}
