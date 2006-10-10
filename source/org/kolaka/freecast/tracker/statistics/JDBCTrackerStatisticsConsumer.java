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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;

public class JDBCTrackerStatisticsConsumer implements TrackerStatisticsConsumer, JDBCTrackerStatisticsConsumerConstants {

  public static final String DEFAULT_REQUEST = "insert into statistics (timestamp,networkid,node_connections,rootnode_connections,listeners,rootnode) values (?,?,?,?,?,?);";


  public static final String[] DEFAULT_PARAMETERS = new String[] {
      PARAMETER_TIMESTAMP, PARAMETER_NETWORKID, PARAMETER_NODECONNECTIONS, 
      PARAMETER_ROOTNODECONNECTIONS, PARAMETER_LISTENERS, PARAMETER_ROOTNODE
  }; 
  
  private String user, password, url, driver;
  
  public void setUser(String user) {
    this.user = user;
  }
  
  public String getUser() {
    return user;
  }
  
  public void setDriver(String driver) {
    this.driver = driver;
  }
  
  public String getDriver() {
    return driver;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getUrl() {
    return url;
  }
  
  private String request = DEFAULT_REQUEST;
  
  public void setRequest(String request) {
    this.request = request;
  }
  
  public String getRequest() {
    return request;
  }
  
  private String[] parameters = DEFAULT_PARAMETERS;

  public void setParameters(String[] parameters) {
    this.parameters = parameters;
  }
  
  private JDBCParametersSetter parametersSetter;
  
  private Connection connection;
  
  public void setConnection(Connection connection) {
    this.connection = connection;
  }
  
  public void process(Date date, TrackerStatistics statistics) {
    if (parametersSetter == null) {
      parametersSetter = new JDBCParametersSetter(parameters);
    }
    
    LogFactory.getLog(getClass()).debug("update database with " + statistics);
    try {
      Connection connection = getConnection();
      PreparedStatement statement = connection.prepareStatement(request);
      parametersSetter.setParameters(statement, date, statistics);
      statement.execute();
    } catch (SQLException e) {
      LogFactory.getLog(getClass()).error("can't insert statistics values in database", e);
    }
  }

  private Connection getConnection() throws SQLException {
    if (connection == null) {
      try {
        Class.forName(driver);
      } catch (ClassNotFoundException e) {
        throw new SQLException("Can't load JBDC driver: " + driver);
      }
      connection = DriverManager.getConnection(url,user,password);
    }
    return connection;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
