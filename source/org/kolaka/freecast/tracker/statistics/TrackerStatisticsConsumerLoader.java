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

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

public class TrackerStatisticsConsumerLoader {

  public TrackerStatisticsConsumer load(Configuration configuration) throws ConfigurationException {
    String consumerClass = configuration.getString("class");

    if (consumerClass.equals("file")) {
      FileTrackerStatisticsConsumer consumer = new FileTrackerStatisticsConsumer();
      consumer.setFile(new File(configuration.getString("file")));
      consumer.setPattern(configuration.getString("pattern", FileTrackerStatisticsConsumer.DEFAULT_PATTERN));
      return consumer;
    } else if (consumerClass.equals("database")) {
      JDBCTrackerStatisticsConsumer consumer = new JDBCTrackerStatisticsConsumer();
      consumer.setUrl(configuration.getString("url"));
      consumer.setUser(configuration.getString("user"));
      consumer.setPassword(configuration.getString("password"));
      consumer.setDriver(configuration.getString("driver"));
      
      consumer.setRequest(configuration.getString("request", JDBCTrackerStatisticsConsumer.DEFAULT_REQUEST));
      
      String parameters = configuration.getString("parameters");
      if (parameters != null) {
        consumer.setParameters(StringUtils.split(parameters,','));
      }
      return consumer;
    } else if (consumerClass.equals("exec")) {
      ExecTrackerStatisticsConsumer consumer = new ExecTrackerStatisticsConsumer();
      consumer.setCommand(configuration.getString("command"));
      if (configuration.containsKey("workingDir")) {
        consumer.setWorkingDir(new File(configuration.getString("workingDir")));
      }
      return consumer;
    }

    throw new ConfigurationException("Unknown consumer class : '" + consumerClass + "'");
  }

}
