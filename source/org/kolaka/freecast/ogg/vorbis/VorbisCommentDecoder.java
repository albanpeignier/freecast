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

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

import org.apache.mina.common.ByteBuffer;
import org.kolaka.freecast.ogg.OggPage;

public class VorbisCommentDecoder {
  
  public static final byte[] VORBIS_PATTERN = new byte[] { 'v', 'o', 'r', 'b', 'i', 's' };
  private CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();

  public VorbisComment decode(OggPage page) throws IOException {
    ByteBuffer payload = ByteBuffer.wrap(page.getPayload());
    payload.order(ByteOrder.LITTLE_ENDIAN);
    
    int packetType = payload.get();
         
    if (packetType != 3) {
      return null;
    }

    checkVorbisPattern(payload);
    String vendor = payload.getPrefixedString(4, charsetDecoder);

    VorbisComment packet = new VorbisComment(vendor);

    int userCommentCount = payload.getInt();
    for (int i = 0; i < userCommentCount; i++) {
      String userComment = payload.getPrefixedString(4, charsetDecoder);
      String[] split = userComment.split("=", 2);
      packet.putUserComment(split[0], split[1]);
    }
    
    return packet;
  }

  private void checkVorbisPattern(ByteBuffer payload) throws IOException {
    byte[] vorbisPattern = new byte[6];
    payload.get(vorbisPattern);
    if (!Arrays.equals(vorbisPattern, VORBIS_PATTERN)) {
      throw new IOException("invalid vorbis pattern");
    }
  }
  

}
