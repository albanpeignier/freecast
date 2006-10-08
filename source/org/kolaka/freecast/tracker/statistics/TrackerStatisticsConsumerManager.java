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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Startable;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.Timer;

public class TrackerStatisticsConsumerManager implements Startable {

  private final Set consumers = new HashSet();

  public void add(TrackerStatisticsConsumer consumer) {
    consumers.add(consumer);
  }

  public void remove(TrackerStatisticsConsumer consumer) {
    consumers.remove(consumer);
  }

  private TrackerStatisticsSetProvider provider;

  public void setProvider(TrackerStatisticsSetProvider provider) {
    this.provider = provider;
  }

  private Timer timer = DefaultTimer.getInstance();

  private int delay = 60;

  private Task task;
  
  public void setDelay(int delay) {
    Validate.isTrue(delay >= 30, "Delay must be greather than 30 seconds");
    this.delay = delay;
  }

  public void start() throws ControlException {
    if (provider == null) {
      LogFactory.getLog(getClass()).info("no statistics provider, manager doesn't start");
      return;
    }
    
    task = new Task() {
      public void run() {
        sendStatistics();
      }
    };
    timer.executePeriodically(delay * 1000, task, false);
  }

  public void stop() throws ControlException {
    if (task != null) {
      task.cancel();
    }
  }

  public void sendStatistics() {
    Date date = new Date();
    for (Iterator iterator = provider.getStatisticsSet().iterator(); iterator
        .hasNext();) {
      TrackerStatistics statistics = (TrackerStatistics) iterator.next();
      sendStatistics(date, statistics);
    }
  }

  private void sendStatistics(Date date, TrackerStatistics statistics) {
    for (Iterator iterator = consumers.iterator(); iterator.hasNext();) {
      TrackerStatisticsConsumer consumer = (TrackerStatisticsConsumer) iterator
          .next();
      consumer.process(date, statistics);
    }
  }

}
