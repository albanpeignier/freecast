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

package org.kolaka.freecast.tracker;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;

public class IceStatsServlet extends HttpServlet {

  private static final String NETWORKID_PARAMETER = "networkId";

  public static final String STATSPROVIDER_ATTRIBUTE = "statsprovider";
  
  private static final long serialVersionUID = -3086504649544196236L;

  private Object provider;
  
  public void init() throws ServletException {
    super.init();
    provider = getServletContext().getAttribute(STATSPROVIDER_ATTRIBUTE);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    TrackerStatistics statistics;
    
    if (provider instanceof MultiTrackerStatisticsProvider) {
      NetworkIdentifier networkId = null;
      
      try {
        String networkIdParameter = request.getParameter(NETWORKID_PARAMETER);
        if (networkIdParameter != null) {
          networkId = NetworkIdentifier.getInstance(networkIdParameter);
        }
      } catch (RuntimeException e) {
        LogFactory.getLog(getClass()).error("Can't find a network identifier in the request", e);
      }

      if (networkId == null) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      statistics = ((MultiTrackerStatisticsProvider) provider).getStatistics(networkId);
    } else {
      statistics = ((TrackerStatisticsProvider) provider).getStatistics();
    }
    
    response.getOutputStream().println(getXMLStatistics(statistics));
  }
  
  private String getXMLStatistics(TrackerStatistics statistics) {
    StringBuffer xml = new StringBuffer("<?xml version=\"1.0\"?>");
    
    int clientConnections = statistics.getNodeConnections() - statistics.getRootNodeConnections(); 
    xml.append(
        "<icestats>" +
        "<client_connections>" + clientConnections + "</client_connections>" +
        "<connections>" + statistics.getNodeConnections() + "</connections>" +
        "<source_connections>" + statistics.getRootNodeConnections() + "</source_connections>" +
        "<sources>" + (statistics.isRootNodePresents() ? 1 : 0) + "</sources>");

    if (statistics.isRootNodePresents()) {
      xml.append("<source mount=\"/rootnode\">" +
        "<listeners>" + statistics.getListenerConnected() + "</listeners>" +
        "<public>0</public>" +
        "<type>Ogg Vorbis</type>" +
        "</source>");
    }
    xml.append("</icestats>");

    return xml.toString();
  }
  
  
}
