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
package org.kolaka.freecast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class Version {

	private final String name;

	private Version() {
		Properties properties = new Properties();
		InputStream resource = getClass().getResourceAsStream("resources/version.properties");
		try {
			if (resource == null) {
			    throw new IOException("can't find the version resources");
			}
			properties.load(resource);
		} catch (IOException e) {
			IllegalStateException exception = new IllegalStateException("can't load version information");
			exception.initCause(e);
			throw exception;
		}

		name = properties.getProperty("name");
		if (name == null) {
			throw new IllegalStateException("no version name found");
		}
	}

	public String getName() {
		return name;
	}

	private static final Version instance = new Version();

	public static Version getInstance() {
		return instance;
	}

}
