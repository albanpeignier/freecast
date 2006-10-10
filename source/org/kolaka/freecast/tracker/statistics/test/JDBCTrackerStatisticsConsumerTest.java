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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.easymock.MockControl;
import org.kolaka.freecast.test.BaseTestCase;
import org.kolaka.freecast.tracker.NetworkIdentifier;
import org.kolaka.freecast.tracker.statistics.DefaultTrackerStatistics;
import org.kolaka.freecast.tracker.statistics.JDBCTrackerStatisticsConsumer;

public class JDBCTrackerStatisticsConsumerTest extends BaseTestCase {

  private JDBCTrackerStatisticsConsumer consumer;
  private DefaultTrackerStatistics statistics;
  private Date date;
  
  protected void setUp() throws Exception {
    super.setUp();
    
    consumer = new JDBCTrackerStatisticsConsumer();
    statistics = new DefaultTrackerStatistics();
    date = new Date();
  }
  
  public void testProcessDefault() throws SQLException {
    String request = "request";
    String parameters = "timestamp,networkid,node_connections,rootnode_connections,listeners,rootnode";
    StatementSetup setup = new StatementSetup() {
      public void setup(PreparedStatement statement) throws SQLException {
        statement.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
        statement.setString(2, statistics.getNetworkId() != null ? statistics.getNetworkId().toString() : null);
        statement.setInt(3, statistics.getNodeConnections());
        statement.setInt(4, statistics.getRootNodeConnections());
        statement.setInt(5, statistics.getListenerConnected());
        statement.setBoolean(6, statistics.isRootNodePresents());
      }
    };
    
    testProcess(request, parameters, setup);
  }
  
  public void testProcessAdvanced() throws SQLException {
    statistics.setNetworkId(NetworkIdentifier.getInstance("1"));
    String request = "insert into statistics select last_insert_id(), ?, ?, ?, ?, ?, ?, id from networks where networkid = ?";
    String parameters = "timestamp,networkid,node_connections,rootnode_connections,listeners,rootnode,networkid";
    StatementSetup setup = new StatementSetup() {
      public void setup(PreparedStatement statement) throws SQLException {
        statement.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
        statement.setString(2, statistics.getNetworkId().toString());
        statement.setInt(3, statistics.getNodeConnections());
        statement.setInt(4, statistics.getRootNodeConnections());
        statement.setInt(5, statistics.getListenerConnected());
        statement.setBoolean(6, statistics.isRootNodePresents());
        statement.setString(7, statistics.getNetworkId().toString());
      }
    };
    
    testProcess(request, parameters, setup);
  }  

  private void testProcess(String request, String parameters, StatementSetup setup) throws SQLException {
    consumer.setRequest(request);
    consumer.setParameters(StringUtils.split(parameters, ','));

    MockControl statementControl = MockControl.createControl(PreparedStatement.class);
    PreparedStatement statement = (PreparedStatement) statementControl.getMock();
    
    setup.setup(statement);
    
    statement.execute();
    statementControl.setReturnValue(true);
    
    statementControl.replay();
    
    MockControl connectionControl = MockControl.createControl(Connection.class);
    Connection connection = (Connection) connectionControl.getMock();

    connection.prepareStatement(request);
    connectionControl.setReturnValue(statement);
    
    connectionControl.replay();
    
    consumer.setConnection(connection);
    consumer.process(date, statistics);

    statementControl.verify();
    connectionControl.verify();
  }
  
  static interface StatementSetup {
    
    void setup(PreparedStatement statement) throws SQLException;
    
  }
  
}
