/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2005 Alban Peignier
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
package org.kolaka.freecast.swing;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.config.Configurations;
import org.kolaka.freecast.resource.ClassResourceLocator;
import org.kolaka.freecast.resource.CompositeResourceLocator;
import org.kolaka.freecast.resource.ResourceLocator;
import org.kolaka.freecast.resource.ResourceLocators;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ConfigurableResources implements Resources {

	private final DataConfiguration configuration;

	private ResourceLocator resourceLocator = ResourceLocators
			.getDefaultInstance();

	public ConfigurableResources(Configuration configuration) {
		DataConfiguration dataConfiguration = new DataConfiguration(
				configuration);
		dataConfiguration.setThrowExceptionOnMissing(true);
		this.configuration = dataConfiguration;

		setResourceLocator(ResourceLocators.getDefaultInstance());
	}
	
	public Resources subset(String name) {
		return new ConfigurableResources(Configurations.subset(configuration,name));
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		Validate.notNull(resourceLocator);

		CompositeResourceLocator locator = new CompositeResourceLocator();
		locator.add(new ClassResourceLocator(getClass()));
		locator.add(resourceLocator);
		this.resourceLocator = locator;
	}

	private String getString(String name) throws ResourcesException {
		try {
			return configuration.getString(name);
		} catch (NoSuchElementException e) {
			throw new ResourcesException(
					"Can't find the configuration value for '" + name + "'", e);
		}
	}

	public String getText(String name) throws ResourcesException {
		return getString(name);
	}

	private ImageIcon getImageIcon(String name) throws ResourcesException {
		LogFactory.getLog(getClass()).debug(
				"load ImageIcon designated by '" + name + "'");
		String resourceName = getString(name);
		URI resourceURI;
		try {
			resourceURI = new URI(resourceName);
		} catch (URISyntaxException e) {
			throw new ResourcesException("Invalid configuration URI " + name
					+ "=" + resourceName, e);
		}

		LogFactory.getLog(getClass()).debug(
				"load ImageIcon at '" + resourceURI + "'");

		try {
			InputStream inputStream = resourceLocator.openResource(resourceURI);
			byte[] data = IOUtils.toByteArray(inputStream);
			return new ImageIcon(data);
		} catch (IOException e) {
			throw new ResourcesException("Can't the image at " + resourceURI, e);
		}
	}

	public Image getImage(String name) throws ResourcesException {
		return getImageIcon(name).getImage();
	}

	public Icon getIcon(String name) throws ResourcesException {
		return getImageIcon(name);
	}

	public Color getColor(String name) throws ResourcesException {
		try {
			return configuration.getColor(name);
		} catch (NoSuchElementException e) {
			throw new ResourcesException("Can't find the color " + name, e);
		}
	}

}
