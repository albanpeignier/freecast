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

package org.kolaka.freecast.ogg.vorbis.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.test.OggTestResources;
import org.kolaka.freecast.ogg.vorbis.VorbisComment;
import org.kolaka.freecast.ogg.vorbis.VorbisCommentInputStream;
import org.kolaka.freecast.ogg.vorbis.VorbisCommentInputStream.CommentHandler;
import org.kolaka.freecast.test.BaseTestCase;

public class VorbisCommentInputStreamTest extends BaseTestCase {

  public void testDecode() throws IOException {
    byte[] expectedChecksum = DigestUtils.md5(IOUtils.toByteArray(loadResource()));
    
    VorbisCommentInputStream input = new VorbisCommentInputStream(loadResource());
    final List vorbisComments = new LinkedList();
    
    CommentHandler handler = new CommentHandler() {
      public void commentRead(VorbisComment comment) {
          vorbisComments.add(comment);
      }
    };
    input.setCommentHandler(handler);

    byte[] checksum = DigestUtils.md5(IOUtils.toByteArray(input));
    assertTrue("invalid checksum", Arrays.equals(expectedChecksum, checksum));

    LogFactory.getLog(getClass()).debug(vorbisComments);
    assertEquals(1, vorbisComments.size());
  }

  /**
   * @return
   */
  private InputStream loadResource() {
    return OggTestResources.getResourceAsStream("sample.ogg");
  }
  
}
