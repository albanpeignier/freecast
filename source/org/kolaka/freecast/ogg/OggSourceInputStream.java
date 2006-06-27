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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.Validate;
import org.apache.mina.common.ByteBuffer;

public class OggSourceInputStream extends InputStream {
  
  private final OggSource source;

  public OggSourceInputStream(OggSource source) {
    Validate.notNull(source);
    this.source = source;
  }

  private ByteBuffer buffer;
  
  public int read() throws IOException {
    if (buffer == null || !buffer.hasRemaining()) {
      try {
        buffer = createNewBuffer();
      } catch (EOFException e) {
        return -1;
      }      
    }
    
    return buffer.getUnsigned();
  }
  
  public void close() throws IOException {
    source.close();
    super.close();
  }

  private ByteBuffer createNewBuffer() throws IOException {
    OggPage page = source.next();
    return ByteBuffer.wrap(page.getRawBytes());
  }

}
