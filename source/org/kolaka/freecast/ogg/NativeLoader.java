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

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.LogFactory;

public class NativeLoader {

  public static void load() {
    if (SystemUtils.IS_OS_WINDOWS) {
      try {
        LogFactory.getLog(NativeLoader.class).debug("pre-load cygogg");
        System.loadLibrary("cygogg-0");
        LogFactory.getLog(NativeLoader.class).debug("pre-load cygvorbis");
        System.loadLibrary("cygvorbis-0");
      } catch (Throwable t) {
        LogFactory.getLog(NativeLoader.class).warn("can't load Ogg/Vorbis library, ogg encoding may be unavailable");
      }
    }
  }
  
}
