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

package org.kolaka.freecast.ogg;

import java.io.InputStream;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang.Validate;

public class OggInputStream extends ProxyInputStream {
  
  private final ProxyOggSource oggSource;

  private OggInputStream(InputStream input, ProxyOggSource oggSource) {
    super(input);
    this.oggSource = oggSource;
  }
  
  public void setPageHandler(ProxyOggSource.PageHandler handler) {
    oggSource.setHandler(handler);
  }
  
  public static OggInputStream getInstance(InputStream input) {
    Validate.notNull(input);
    
    ProxyOggSource filteredOggSource = new ProxyOggSource(new OggStreamSource(input));
    InputStream filtered = new OggSourceInputStream(filteredOggSource);
    return new OggInputStream(filtered, filteredOggSource);
  }

}
