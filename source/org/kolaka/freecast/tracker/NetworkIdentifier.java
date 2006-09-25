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

import java.io.Serializable;
import java.util.Random;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.math.NumberUtils;

public class NetworkIdentifier implements Comparable, Serializable {

  private static final long serialVersionUID = 2257191674286252870L;
  private final long value;

  private NetworkIdentifier(long value) {
    this.value = value;
  }
  
  public static NetworkIdentifier getInstance(String value) {
    return new NetworkIdentifier(Long.parseLong(value, 16));
  }

  private static Random random = new Random();
  
  public static NetworkIdentifier getRandomInstance() {
    return new NetworkIdentifier(random.nextLong());
  }

  public String toString() {
    return "#" + Long.toHexString(value).toUpperCase();
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public int compareTo(Object object) {
    NetworkIdentifier other = (NetworkIdentifier) object;
    return NumberUtils.compare(value, other.value);
  }

}
