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

package org.kolaka.freecast.config;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.resource.ResourceLocator;
import org.kolaka.freecast.resource.ResourceLocators;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class DefaultConfigurationLoader implements ConfigurationLoader {
  
	private final String defaultsName;

	private URI commandLineURI;

  private File userFile = new File(new File(SystemUtils.USER_HOME, ".freecast"), "config.xml");
  
	protected Properties commandLineProperties = new Properties();

	protected HierarchicalConfiguration configuration;

	private ResourceLocator locator = ResourceLocators.getDefaultInstance();

	public void setResourceLocator(ResourceLocator locator) {
		this.locator = locator;
	}
  
	public void setCommandLineURI(URI commandLineURI) {
		this.commandLineURI = commandLineURI;
	}
  
	public void addCommandLineProperty(String property, String value) {
		commandLineProperties.put(property, value);
	}

	public DefaultConfigurationLoader(String defaultsName) {
		this.defaultsName = defaultsName;
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

	protected AbstractConfiguration loadCommandLineConfiguration()
			throws ConfigurationException {
		if (commandLineURI == null) {
			return new BaseConfiguration();
		}

		LogFactory.getLog(getClass()).debug(
				"load the command line configuration from " + commandLineURI);
		XMLConfiguration configuration = new XMLConfiguration();
		try {
			configuration.load(locator.openResource(commandLineURI));
		} catch (ResourceLocator.Exception e) {
			throw new ConfigurationException(
					"Can't load the command line configuration URI " + commandLineURI, e);
		}
		return configuration;
	}
  
  protected XMLConfiguration loadUserConfiguration()
  throws ConfigurationException {
    LogFactory.getLog(getClass()).debug(
        "load the user configuration from " + userFile);
    XMLConfiguration configuration = new XMLConfiguration(userFile);
    
    if (userFile.exists())
      try {
        configuration.load();
      } catch (ConfigurationException e) {
        LogFactory.getLog(getClass()).info("no user configuration found");
        LogFactory.getLog(getClass()).debug(
            "can't load user configuration from " + userFile, e);
      } 
    else {
      configuration.setRootElementName("freecast");
    }
    
    return configuration;
  }
  
  public Configuration getUserConfiguration() {
    return userConfiguration;
  }
  
  public void saveUserConfiguration() throws ConfigurationException {
    userFile.getParentFile().mkdirs();
    userConfiguration.save();
  }

	public HierarchicalConfiguration getRootConfiguration() {
		return configuration;
	}
  
  private static final AbstractConfiguration EMPTY_CONFIGURATION = new PropertiesConfiguration();

  private XMLConfiguration userConfiguration;

	protected AbstractConfiguration loadDefaultConfiguration(String name)
			throws ConfigurationException {
		URL url = getClass().getResource("resources/config-" + name + ".xml");
		if (url == null) {
      LogFactory.getLog(getClass()).warn("Can't find the default configuration settings:" + name);
      return EMPTY_CONFIGURATION;
		}
		LogFactory.getLog(getClass()).debug(
				"load the default configuration from " + url);
    DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder(url);
    AbstractConfiguration configuration = factory.getConfiguration(true);
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

	protected HierarchicalConfiguration loadConfiguration() throws ConfigurationException {
    CombinedConfiguration configuration = new CombinedConfiguration();
    // two special rules for Configuration
    configuration.setThrowExceptionOnMissing(true);
    configuration.setDelimiterParsingDisabled(true);

    if (!commandLineProperties.isEmpty()) {
			LogFactory.getLog(getClass()).trace("use command line properties: " + commandLineProperties);
			configuration
					.addConfiguration(new MapConfiguration(commandLineProperties));
		}
		configuration.addConfiguration(loadCommandLineConfiguration());
    userConfiguration = loadUserConfiguration();
    configuration.addConfiguration(userConfiguration);
		completeConfiguration(configuration);

    
		return configuration;
	}

	protected void completeConfiguration(CombinedConfiguration configuration)
			throws ConfigurationException {
		configuration.addConfiguration(loadDefaultConfiguration(defaultsName));
	}

}
