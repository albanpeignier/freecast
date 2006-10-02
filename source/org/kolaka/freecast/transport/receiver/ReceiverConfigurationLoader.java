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

package org.kolaka.freecast.transport.receiver;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.config.InetSocketAddressConfigurator;

public class ReceiverConfigurationLoader {

  public ReceiverConfiguration load(DataConfiguration receiverConfiguration)
      throws ConfigurationException {
    String receiverClass = receiverConfiguration.getString("class");

    if (receiverClass.equals("shoutclient")) {
      ShoutClientReceiverConfiguration configuration = new ShoutClientReceiverConfiguration();
      configuration.setUrl(receiverConfiguration.getURL("url"));
      return configuration;
    }

    if (receiverClass.equals("peer")) {
      return new PeerReceiverConfiguration();
    }

    if (receiverClass.equals("test")) {
      return new TestReceiverConfiguration();
    }

    if (receiverClass.equals("shoutserver")) {
      ShoutServerReceiverConfiguration configuration = new ShoutServerReceiverConfiguration();
      configuration.setListenAddress(new InetSocketAddressConfigurator()
          .load(receiverConfiguration.subset("listenaddress")));
      return configuration;
    }

    if (receiverClass.equals("playlist")
        || receiverClass.equals("encoder-playlist")) {
      String playlistURIString = receiverConfiguration.getString("url");
      URI playlistURI = null;
      try {
        playlistURI = new URI(playlistURIString);
      } catch (URISyntaxException e) {
        throw new ConfigurationException("invalid playlist url: '"
            + playlistURIString, e);
      }

      if (receiverClass.equals("playlist")) {
        PlaylistReceiverConfiguration configuration = new PlaylistReceiverConfiguration();
        configuration.setUri(playlistURI);
        configuration.setBandwidth(receiverConfiguration
            .getInt("bandwidth", 40));

        LogFactory.getLog(getClass()).warn(
            "the playlist receiver uses a static bandwidth controler at "
                + configuration.getBandwidth());
        return configuration;
      } else {
        PlaylistEncoderReceiverConfiguration configuration = new PlaylistEncoderReceiverConfiguration();

        int channels = receiverConfiguration.getInt("channels",
            EncoderFormat.DEFAULT.getChannels());
        int sampleRate = receiverConfiguration.getInt("sampleRate",
            EncoderFormat.DEFAULT.getSampleRate());
        float quality = receiverConfiguration.getFloat("quality",
            EncoderFormat.DEFAULT.getQuality());

        configuration.setEncoderFormat(new EncoderFormat(channels, sampleRate,
            quality));
        configuration.setUri(playlistURI);
        return configuration;
      }
    }

    throw new ConfigurationException("Unknown receiver class: '"
        + receiverClass + "'");
  }

}
