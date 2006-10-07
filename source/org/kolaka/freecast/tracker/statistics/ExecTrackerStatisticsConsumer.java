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
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;

public class ExecTrackerStatisticsConsumer implements TrackerStatisticsConsumer {

  private File workingDir;
  private String command;
  
  public void setWorkingDir(File workingDir) {
    this.workingDir = workingDir;
  }
  
  public void setCommand(String command) {
    this.command = command;
  }

  public void process(Date date, TrackerStatistics statistics) {
    if (StringUtils.isEmpty(command)) {
      LogFactory.getLog(getClass()).error("No specified command");
      return;
    }
    
    StringBuffer execCommand = new StringBuffer(command);
    execCommand.append(' ').append(date.getTime() / 1000);
    execCommand.append(' ').append(statistics.getNetworkId() != null ? statistics.getNetworkId().toString() : "none");
    execCommand.append(' ').append(statistics.getNodeConnections());
    execCommand.append(' ').append(statistics.getRootNodeConnections());
    execCommand.append(' ').append(statistics.getListenerConnected());
    execCommand.append(' ').append(statistics.isRootNodePresents());
     
    LogFactory.getLog(getClass()).debug("execute '" + execCommand + "' in " + workingDir);
    executor.execute(execCommand.toString(), workingDir);
  }
  
  private Executor executor = new Executor() {
    public void execute(String command, File workingDir) {
      try {
        if (workingDir != null) {
          Runtime.getRuntime().exec(command, new String[0], workingDir);
        } else {
          Runtime.getRuntime().exec(command);
        }
      } catch (IOException e) {
        LogFactory.getLog(getClass()).error("Can't execute '" + command + "' in " + workingDir, e);
      }
    }
  };
  
  public void setExecutor(Executor executor) {
    Validate.notNull(executor);
    this.executor = executor;
  }
  
  public static interface Executor {
    
    void execute(String command, File workingDir);
    
  }
  
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
