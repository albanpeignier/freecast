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

package org.kolaka.freecast.player;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.vorbis.VorbisComment;

public class VorbisPlayDescriptionFactory {

  private static final String[] KEYS = new String[] { VorbisComment.ARTIST, VorbisComment.TITLE, VorbisComment.ALBUM, VorbisComment.TRACKNUMBER };

  public PlayDescription create(VorbisComment comment) {
    String shortDescription = createShortDescription(comment);
    PlayDescription playDescription = new PlayDescription(shortDescription);
    playDescription.setLongDescription(createLongDescription(comment));
    playDescription.setUrl(createURL(comment));
    return playDescription;
  }

  private URL createURL(VorbisComment comment) {
    String www = comment.getUserComment(VorbisComment.WWW);
    if (StringUtils.isEmpty(www)) {
      return null;
    }
    
    try {
      return new URL(www);
    } catch (MalformedURLException e) {
      LogFactory.getLog(getClass()).error("Can't parse URL '" + www + "'");
      return null;
    }    
  }

  private String createShortDescription(VorbisComment comment) {
    StringBuffer description = new StringBuffer();
    for (int i = 0; i < KEYS.length; i++) {
      String value = comment.getUserComment(KEYS[i]);
      if (value != null) {
        if (description.length() > 0) {
          description.append(" - ");
        }
        description.append(value);
      }
    }
    return description.toString();
  }

  private String createLongDescription(VorbisComment comment) {
    StringBuffer description = new StringBuffer("<html>");
    boolean empty = true;
    for (int i = 0; i < KEYS.length; i++) {
      String key = KEYS[i];
      String value = comment.getUserComment(key);
      if (value != null) {
        if (!empty) {
          description.append("<br>");
        }
        description.append("<b>").append(WordUtils.capitalizeFully(key)).append("</b>").append(": ").append(value);
        empty = false;
      }
    }
    return description.toString();
  }

}
