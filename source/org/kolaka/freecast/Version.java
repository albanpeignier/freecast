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

package org.kolaka.freecast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class Version {

  private static final Version UNDEFINED = new Version("undefined");

	private final String name;

  private Version(String name) {
    this.name = name;
  }

  private static Version load() {
    Properties properties = new Properties();
    InputStream resource = Version.class.getResourceAsStream(
        "resources/version.properties");
    try {
      if (resource == null) {
        throw new IOException("can't find the version resources");
      }
      properties.load(resource);
    } catch (IOException e) {
      LogFactory.getLog(Version.class).warn("can't load version information", e);
      return UNDEFINED;
    }

    String name = properties.getProperty("name");
    if (name == null) {
      LogFactory.getLog(Version.class).warn("no version name found");
      return UNDEFINED;
    }
    return new Version(name);
  }


	public String getName() {
		return name;
	}

	private static final Version INSTANCE = load();

	public static Version getINSTANCE() {
		return INSTANCE;
	}

}
