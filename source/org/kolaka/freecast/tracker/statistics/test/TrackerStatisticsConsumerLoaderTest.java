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
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.kolaka.freecast.tracker.statistics.FileTrackerStatisticsConsumer;
import org.kolaka.freecast.tracker.statistics.JDBCTrackerStatisticsConsumer;
import org.kolaka.freecast.tracker.statistics.TrackerStatisticsConsumerLoader;

public class TrackerStatisticsConsumerLoaderTest extends TestCase {

  private TrackerStatisticsConsumerLoader loader;
  
  protected void setUp() throws Exception {
    loader = new TrackerStatisticsConsumerLoader();
  }

  public void testLoadFile() throws ConfigurationException {
    String file = "stats.log";
    String pattern = "test";
    
    Map map = new TreeMap();
    map.put("class", "file");
    map.put("file", file);
    map.put("pattern", pattern );
    
    FileTrackerStatisticsConsumer consumer = 
      (FileTrackerStatisticsConsumer) loader.load(new MapConfiguration(map));
    
    assertEquals(new File(file), consumer.getFile());
    assertEquals(pattern, consumer.getPattern());
  }

  public void testLoadJdbc() throws ConfigurationException {
    String url = "url";
    String user = "user";
    String password = "password";
    String request = "request";
    String driver = "driver";
    
    Map map = new TreeMap();
    map.put("class", "database");
    map.put("url", url);
    map.put("user", user);
    map.put("password", password);
    map.put("driver", driver);
    map.put("request", request);
    
    JDBCTrackerStatisticsConsumer consumer = 
      (JDBCTrackerStatisticsConsumer) loader.load(new MapConfiguration(map));
    
    assertEquals(url, consumer.getUrl());
    assertEquals(user, consumer.getUser());
    assertEquals(password, consumer.getPassword());
    assertEquals(driver, consumer.getDriver());
    assertEquals(request, consumer.getRequest());
  }

}
