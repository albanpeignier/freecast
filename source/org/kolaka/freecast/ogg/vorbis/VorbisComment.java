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

package org.kolaka.freecast.ogg.vorbis;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

public class VorbisComment {
  
  private final String vendor;
  
  public static final String ARTIST = "ARTIST";
  public static final String TITLE = "TITLE";
  public static final String TRACKNUMBER = "TRACKNUMBER";
  public static final String WWW = "WWW";
  
  private final Map userComments = new TreeMap();
  
  public VorbisComment(final String vendor) {
    Validate.notEmpty(vendor);
    this.vendor = vendor;
  }

  public String getVendor() {
    return vendor;
  }
  
  public void putUserComment(String key, String value) {
    Validate.notEmpty(key);
    userComments.put(key, value);
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
