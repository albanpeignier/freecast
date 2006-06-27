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

import java.io.InputStream;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.OggInputStream;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.ProxyOggSource.PageHandler;

public class VorbisCommentInputStream extends ProxyInputStream {
  
  private CommentHandler handler = new CommentHandler() {
    public void commentRead(VorbisComment comment) {
    }
  };

  public VorbisCommentInputStream(InputStream input) {
    super(OggInputStream.getInstance(input));
    
    final VorbisCommentDecoder decoder = new VorbisCommentDecoder();
    PageHandler pageHandler = new PageHandler() {
      public void pageRead(OggPage page) {
        try {
          VorbisComment comment = decoder.decode(page);
          if (comment != null) {
            handler.commentRead(comment);
          }
        } catch (Exception e) {
          LogFactory.getLog(getClass()).error("Can't decode vorbis comment into " + page, e);
        }        
      }
    };
    ((OggInputStream) in).setPageHandler(pageHandler);
  }
  
  public void setCommentHandler(CommentHandler handler) {
    Validate.notNull(handler);
    this.handler = handler;
  }
  
  public static interface CommentHandler {
    
    void commentRead(VorbisComment comment);
    
  }

}
