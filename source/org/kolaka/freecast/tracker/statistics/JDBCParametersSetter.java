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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;

public class JDBCParametersSetter {

  private final List setters;

  public JDBCParametersSetter(String[] parameters) {
    Validate.notEmpty(parameters);
    setters = createSetters(parameters);
  }
  
  public void setParameters(PreparedStatement statement, Date date, TrackerStatistics statistics) throws SQLException {
    for (ListIterator iter=setters.listIterator(); iter.hasNext(); ) {
      int parameterIndex = iter.nextIndex() + 1;
      Setter setter = (Setter) iter.next();
      LogFactory.getLog(getClass()).trace("set parameter " + parameterIndex + " with " + setter);
      setter.set(statement, parameterIndex, date, statistics);
    }
  }
  
  private List createSetters(String[] parameters) {
    List setters = new LinkedList();
    
    for (int i=0; i < parameters.length; i++) {
      String parameter = parameters[i];
      Setter setter = (Setter) SETTERS.get(parameter);
      if (setter == null) {
        throw new IllegalArgumentException("Invalid parameter " + parameter);
      }
        
      setters.add(setter);
    }
    
    return setters;
  }

  private static final Map SETTERS = new TreeMap();
  
  static {
    register(new Setter(JDBCTrackerStatisticsConsumerConstants.PARAMETER_TIMESTAMP) {
      public void set(PreparedStatement statement, int parameterIndex, Date date, TrackerStatistics statistics) throws SQLException {
        statement.setTimestamp(parameterIndex, new java.sql.Timestamp(date.getTime()));
      }
    });
    register(new Setter(JDBCTrackerStatisticsConsumerConstants.PARAMETER_NETWORKID) {
      public void set(PreparedStatement statement, int parameterIndex, Date date, TrackerStatistics statistics) throws SQLException {
        statement.setString(parameterIndex, statistics.getNetworkId() != null ? statistics.getNetworkId().toString() : null);
      }
    });
    register(new Setter(JDBCTrackerStatisticsConsumerConstants.PARAMETER_NODECONNECTIONS) {
      public void set(PreparedStatement statement, int parameterIndex, Date date, TrackerStatistics statistics) throws SQLException {
        statement.setInt(parameterIndex, statistics.getNodeConnections());
      }
    });
    register(new Setter(JDBCTrackerStatisticsConsumerConstants.PARAMETER_ROOTNODECONNECTIONS) {
      public void set(PreparedStatement statement, int parameterIndex, Date date, TrackerStatistics statistics) throws SQLException {
        statement.setInt(parameterIndex, statistics.getRootNodeConnections());
      }
    });
    register(new Setter(JDBCTrackerStatisticsConsumerConstants.PARAMETER_LISTENERS) {
      public void set(PreparedStatement statement, int parameterIndex, Date date, TrackerStatistics statistics) throws SQLException {
        statement.setInt(parameterIndex, statistics.getListenerConnected());
      }
    });
    register(new Setter(JDBCTrackerStatisticsConsumerConstants.PARAMETER_ROOTNODE) {
      public void set(PreparedStatement statement, int parameterIndex, Date date, TrackerStatistics statistics) throws SQLException {
        statement.setBoolean(parameterIndex, statistics.isRootNodePresents());
      }
    });
  }
  
  static void register(Setter setter) {
    SETTERS.put(setter.getParameter(), setter);
  }
  
  static abstract class Setter {
    
    private String parameter;

    Setter(String parameter) {
      this.parameter = parameter;
    }
    
    public String getParameter() {
      return parameter;
    }
    
    abstract void set(PreparedStatement statement, int parameterIndex, Date date, TrackerStatistics statistics) throws SQLException;
    
    public String toString() {
      return "set " + parameter;
    }
    
  }
  
}
