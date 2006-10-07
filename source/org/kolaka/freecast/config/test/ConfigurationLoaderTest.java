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

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.kolaka.freecast.config.ConfigurationLoader;
import org.kolaka.freecast.config.DefaultConfigurationLoader;

public class ConfigurationLoaderTest extends TestCase {

	public void testTrackerLoad() throws ConfigurationException {
		ConfigurationLoader configurationLoader = new DefaultConfigurationLoader(
				"tracker");
		configurationLoader.load();
		Configuration configuration = configurationLoader
				.getRootConfiguration().subset("tracker");

		assertNull(configuration.getString("dummy"));

		assertEquals("http", configuration.getString("connector.class"));

		Configuration connectorConfiguration = configuration
				.subset("connector");
		assertEquals("0.0.0.0", connectorConfiguration
				.getString("listenaddress.host"));
		assertEquals("1665", connectorConfiguration
				.getString("listenaddress.port"));
	}

}
