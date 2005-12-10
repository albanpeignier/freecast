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

package org.kolaka.freecast.config;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.resource.ResourceLocator;
import org.kolaka.freecast.resource.ResourceLocators;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public abstract class BaseConfigurationLoader implements ConfigurationLoader {

	private URI userURI;

	protected Properties userProperties = new Properties();

	protected Configuration configuration;

	private ResourceLocator locator = ResourceLocators.getDefaultInstance();

	public void setResourceLocator(ResourceLocator locator) {
		this.locator = locator;
	}

	public void setUserURI(URI userURI) {
		this.userURI = userURI;
	}

	public void addUserProperty(String property, String value) {
		userProperties.put(property, value);
	}

	protected void logConfiguration() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = configuration.getKeys(); iter.hasNext();) {
			String key = (String) iter.next();
			sb.append(key).append('=').append(configuration.getString(key));
			if (iter.hasNext()) {
				sb.append(',');
			}
		}
		LogFactory.getLog(getClass()).debug(
				"loaded configuration: " + sb.toString());
	}

	protected Configuration loadUserConfiguration()
			throws ConfigurationException {
		if (userURI == null) {
			return new BaseConfiguration();
		}

		LogFactory.getLog(getClass()).debug(
				"load the user configuration from " + userURI);
		XMLConfiguration configuration = new XMLConfiguration();
		try {
			configuration.load(locator.openResource(userURI));
		} catch (ResourceLocator.Exception e) {
			throw new ConfigurationException(
					"Can't load the user configuration URI " + userURI, e);
		}
		return configuration;
	}

	public Configuration getRootConfiguration() {
		return configuration;
	}

	protected Configuration loadDefaultConfiguration(String name)
			throws ConfigurationException {
		URL url = getClass().getResource("resources/config-" + name + ".xml");
		if (url == null) {
			throw new ConfigurationException(
					"Can't find the default configuration settings:" + name);
		}
		LogFactory.getLog(getClass()).debug(
				"load the default configuration from " + url);
		ConfigurationFactory factory = new ConfigurationFactory();
		factory.setConfigurationURL(url);
		Configuration configuration = factory.getConfiguration();
		if (configuration.isEmpty()) {
			throw new ConfigurationException(
					"No default configuration found for " + url);
		}
		return configuration;
	}

	public void load() throws ConfigurationException {
		if (configuration != null) {
			LogFactory.getLog(getClass()).warn(
					"configuration reloading not supported");
			return;
		}

		this.configuration = loadConfiguration();

		logConfiguration();
	}

	protected Configuration loadConfiguration() throws ConfigurationException {
		CompositeConfiguration configuration = new CompositeConfiguration();
		if (!userProperties.isEmpty()) {
			configuration
					.addConfiguration(new MapConfiguration(userProperties));
		}
		configuration.addConfiguration(loadUserConfiguration());
		completeConfiguration(configuration);

		configuration.setThrowExceptionOnMissing(true);
		return configuration;
	}

	protected abstract void completeConfiguration(
			CompositeConfiguration configuration) throws ConfigurationException;

}
