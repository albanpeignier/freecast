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

package org.kolaka.freecast.transport.receiver;

import java.net.URI;
import java.net.URL;

import org.apache.commons.lang.Validate;
import org.kolaka.freecast.resource.URIs;

public class PlaylistEncoderReceiverConfiguration extends SourceReceiverConfiguration {
	
	private URI uri;
	
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}
  
  private EncoderFormat encoderFormat = EncoderFormat.DEFAULT;

  public EncoderFormat getEncoderFormat() {
    return encoderFormat;
  }
  
  public void setEncoderFormat(EncoderFormat encoderFormat) {
    this.encoderFormat = encoderFormat;
  }
  
	public PlaylistEncoderReceiverConfiguration(URI uri) {
		Validate.notNull(uri, "No specified URI");
		this.uri = uri;
	}
	
	public PlaylistEncoderReceiverConfiguration() {
	}

	public static PlaylistEncoderReceiverConfiguration getInstance(URL url) {
		URI uri = URIs.toURI(url);
		return new PlaylistEncoderReceiverConfiguration(uri);
	}
	
	public static PlaylistEncoderReceiverConfiguration getInstance(Playlist playlist) {
		URI uri = playlist.getDefinitionURI();
		return new PlaylistEncoderReceiverConfiguration(uri);
	}
	
	public void validate() throws ValidateException {
		if (uri == null) {
			throw new ValidateException("No defined URI");
		}
	}
	
}