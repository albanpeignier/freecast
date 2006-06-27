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

package org.kolaka.freecast.ogg.test;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.kolaka.freecast.ogg.OggInputStream;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.ProxyOggSource.PageHandler;

public class OggSourceInputStreamTest extends TestCase {

  public void testRead() throws IOException {
    int expectedLength = IOUtils.toByteArray(loadResource()).length;
    
    InputStream resource = loadResource();

    final MutableInt readPageLength = new MutableInt();
    
    PageHandler handler = new PageHandler() {
      public void pageRead(OggPage page) {
        readPageLength.setValue(readPageLength.intValue() + page.getLength());
      }
    };
    OggInputStream input = OggInputStream.getInstance(resource);
    input.setPageHandler(handler);

    int readLength = IOUtils.toByteArray(input).length;
    assertEquals(expectedLength, readLength);
    
    assertEquals(expectedLength, readPageLength.intValue());
  }

  private InputStream loadResource() {
    return OggTestResources.getResourceAsStream("sample.ogg");
  }

}
