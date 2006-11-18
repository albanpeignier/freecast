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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;

public class FileTrackerStatisticsConsumer implements TrackerStatisticsConsumer {

  public static final String DEFAULT_PATTERN = "{0,date,yyyyMMdd-HHmmss} {1} {2} {3} {4} {5}";
  private File file;
  
  public void setFile(File file) {
    this.file = file;
  }
  
  public File getFile() {
    return file;
  }

  private String pattern = DEFAULT_PATTERN;

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
  
  public String getPattern() {
    return pattern;
  }
  
  public void process(Date date, TrackerStatistics statistics) {
    LogFactory.getLog(getClass()).debug("append statistics to " + file);
    PrintWriter writer = createWriter();
    writer.println(format(date, statistics));
    writer.close();
  }

  private MessageFormat messageFormat;
  
  private String format(Date date, TrackerStatistics statistics) {
    if (messageFormat == null) {
      messageFormat = new MessageFormat(pattern);
    }
    Object[] arguments = new Object[] {
        date,
        statistics.getNetworkId(),
        new Integer(statistics.getNodeConnections()),
        new Integer(statistics.getRootNodeConnections()),
        new Integer(statistics.getListenerConnected()),
        new Boolean(statistics.isRootNodePresents())
    };
    return messageFormat.format(arguments);
  }

  private PrintWriter createWriter() {
    if (file == null) {
      LogFactory.getLog(getClass()).error("no specified output file");
    }
    
    try {
      return new PrintWriter(new FileWriter(file,true));
    } catch (IOException e) {
      LogFactory.getLog(getClass()).error("can't create a writer to " + file, e);
      return new PrintWriter(new StringWriter());
    }
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
