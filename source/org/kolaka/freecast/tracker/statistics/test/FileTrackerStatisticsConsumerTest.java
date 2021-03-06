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
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.kolaka.freecast.tracker.NetworkIdentifier;
import org.kolaka.freecast.tracker.statistics.DefaultTrackerStatistics;
import org.kolaka.freecast.tracker.statistics.FileTrackerStatisticsConsumer;

public class FileTrackerStatisticsConsumerTest extends TestCase {

  private File file;
  private FileTrackerStatisticsConsumer consumer;

  protected void setUp() throws Exception {
    super.setUp();
    file = File.createTempFile(getClass().getName(), "test");

    consumer = new FileTrackerStatisticsConsumer();
    consumer.setFile(file);
}
  
  protected void tearDown() throws Exception {
    super.tearDown();
    file.delete();
  }
  
  public void testProcess() throws IOException {
    DefaultTrackerStatistics stats = new DefaultTrackerStatistics(1, 2, true, 3);
    stats.setNetworkId(NetworkIdentifier.getInstance("0"));
    consumer.process(new Date(0), stats);
    
    List lines = IOUtils.readLines(new FileReader(file));
    assertEquals(1, lines.size());
    String output = (String) lines.get(0);
    // TODO could fail in another timezone :/
    assertEquals("19700101-010000 0 1 2 3 true", output);
  }

}
