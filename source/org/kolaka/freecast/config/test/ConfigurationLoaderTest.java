/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2006 Alban Peignier
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

package org.kolaka.freecast.config.test;

import java.util.NoSuchElementException;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.kolaka.freecast.config.ConfigurationLoader;
import org.kolaka.freecast.config.Configurations;
import org.kolaka.freecast.config.DefaultConfigurationLoader;
import org.kolaka.freecast.test.BaseTestCase;

public class ConfigurationLoaderTest extends BaseTestCase {

  public void testTrackerLoad() throws ConfigurationException {
    ConfigurationLoader configurationLoader = new DefaultConfigurationLoader("tracker");
    configurationLoader.load();

    HierarchicalConfiguration rootConfiguration = configurationLoader.getRootConfiguration();
    assertTrue(rootConfiguration.isThrowExceptionOnMissing());
    testNoSuchElementException(rootConfiguration);

    Configuration configuration = Configurations.subset(rootConfiguration, "tracker");

    testNoSuchElementException(configuration);

    testNoSuchElementException(configuration);

    assertEquals("http", configuration.getString("connector.class"));

    Configuration connectorConfiguration = Configurations.subset(configuration, "connector");
    assertEquals("0.0.0.0", connectorConfiguration.getString("listenaddress.host"));
    assertEquals("1665", connectorConfiguration.getString("listenaddress.port"));
  }

  private void testNoSuchElementException(Configuration configuration) {
    assertTrue(configuration + " doesn't ThrowExceptionOnMissing", ((AbstractConfiguration) configuration).isThrowExceptionOnMissing());
    
    try {
      configuration.getString("dummy");
      fail(configuration + " should throw a NoSuchElementException");
    } catch (NoSuchElementException e) {

    }
  }

}
