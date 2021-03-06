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

package org.kolaka.freecast.lang;

public class ArrayUtils {

  public static int indexOf(byte[] array, byte[] pattern) {
    if (array.length < pattern.length) {
      return -1;
    }

    for (int i=0; i < (array.length - pattern.length); i++) {
      if (isPatternFoundAt(array, i, pattern)) {
        return i;
      }
    }

    return -1;
  }

  private static boolean isPatternFoundAt(byte array[], int position, byte[] pattern) {
    if (array.length < position + pattern.length) {
      return false;
    }

    for (int i=0; i < pattern.length; i++) {
      if (array[position + i] != pattern[i]) {
        return false;
      }
    }

    return true;
  }


}
