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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.kolaka.freecast.ogg.OggSourceInputStream;
import org.kolaka.freecast.ogg.OggStreamSource;

public class OggInputStreamTest extends TestCase {

  public void testRead() throws IOException {
    byte[] expected = IOUtils.toByteArray(loadResource());
    byte[] expectedChecksum = DigestUtils.md5(expected);
    
    OggStreamSource oggSource = new OggStreamSource(loadResource());
    OggSourceInputStream input = new OggSourceInputStream(oggSource);

    byte[] read = IOUtils.toByteArray(input);
    byte[] checksum = DigestUtils.md5(read);
    
    // HexDump.dump(expected, 0, new FileOutputStream("expected.dump"), 0);
    // HexDump.dump(read, 0, new FileOutputStream("read.dump"), 0);
    assertTrue("invalid checksum", Arrays.equals(expectedChecksum, checksum));
  }

  private InputStream loadResource() {
    return OggTestResources.getResourceAsStream("sample.ogg");
  }

}
