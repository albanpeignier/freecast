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

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.config.Configurations;
import org.kolaka.freecast.config.InetSocketAddressConfigurator;

public class ReceiverConfigurationLoader {
  
  private static final String PROPERTY_QUALITY = "quality";
  private static final String PROPERTY_SAMPLERATE = "sampleRate";
  private static final String PROPERTY_CHANNELS = "channels";
  private static final String CLASS_PLAYLISTENCODER = "encoder-playlist";
  private static final String PROPERTY_BANDWIDTH = "bandwidth";
  private static final String CLASS_PLAYLIST = "playlist";
  private static final String CLASS_SHOUTSERVER = "shoutserver";
  private static final String CLASS_TEST = "test";
  private static final String CLASS_PEER = "peer";
  private static final String CLASS_SHOUTCLIENT = "shoutclient";

  private static final String PROPERTY_CLASS = "class";
  private static final String PROPERTY_URL = "url";
  private static final String PROPERTY_LISTENADDRESS = "listenaddress";
  
  private static final String PREFIX = "node.receiver.";
  
  private final InetSocketAddressConfigurator inetSocketAddressConfigurator = new InetSocketAddressConfigurator();

  /**
   * 
   * TODO subset(...).setProperty doesn't work for HierarchicalConfiguration
   */
  public void save(ReceiverConfiguration configuration, HierarchicalConfiguration receiverConfiguration) {
    receiverConfiguration.clearTree(PREFIX);
    
    if (configuration instanceof ShoutClientReceiverConfiguration) {
      receiverConfiguration .setProperty(PREFIX + PROPERTY_CLASS, CLASS_SHOUTCLIENT);
      ShoutClientReceiverConfiguration shoutClientReceiverConfiguration = (ShoutClientReceiverConfiguration) configuration;
      receiverConfiguration.setProperty(PREFIX + PROPERTY_URL, shoutClientReceiverConfiguration.getUrl());
    } else if (configuration instanceof PeerReceiverConfiguration) {
      receiverConfiguration.setProperty(PREFIX + PROPERTY_CLASS, CLASS_PEER);
    } else if (configuration instanceof TestReceiverConfiguration) {
      receiverConfiguration.setProperty(PREFIX + PROPERTY_CLASS, CLASS_TEST);
    } else if (configuration instanceof ShoutServerReceiverConfiguration) {
      receiverConfiguration.setProperty(PREFIX + PROPERTY_CLASS, CLASS_SHOUTSERVER);
      ShoutServerReceiverConfiguration shoutServerReceiverConfiguration = (ShoutServerReceiverConfiguration) configuration;

      InetSocketAddress address = shoutServerReceiverConfiguration.getListenAddress();
      String addressPrefix = PREFIX + PROPERTY_LISTENADDRESS + ".";
      
      receiverConfiguration.setProperty(addressPrefix + InetSocketAddressConfigurator.PROPERTY_HOST, address.getHostName());
      receiverConfiguration.setProperty(addressPrefix + InetSocketAddressConfigurator.PROPERTY_PORT, new Integer(address.getPort()));
      
    } else if (configuration instanceof PlaylistReceiverConfiguration) {
      receiverConfiguration.setProperty(PREFIX + PROPERTY_CLASS, CLASS_PLAYLIST);
      PlaylistReceiverConfiguration playlistReceiverConfiguration = (PlaylistReceiverConfiguration) configuration;
      receiverConfiguration.setProperty(PREFIX + PROPERTY_URL, playlistReceiverConfiguration.getUri());
      receiverConfiguration.setProperty(PREFIX + PROPERTY_BANDWIDTH, new Integer(playlistReceiverConfiguration.getBandwidth()));
    } else if (configuration instanceof PlaylistEncoderReceiverConfiguration) {
      receiverConfiguration.setProperty(PREFIX + PROPERTY_CLASS, CLASS_PLAYLISTENCODER);
      PlaylistEncoderReceiverConfiguration playlistEncoderReceiverConfiguration = (PlaylistEncoderReceiverConfiguration) configuration;
      receiverConfiguration.setProperty(PREFIX + PROPERTY_URL, playlistEncoderReceiverConfiguration.getUri());
      EncoderFormat encoderFormat = playlistEncoderReceiverConfiguration.getEncoderFormat();
      receiverConfiguration.setProperty(PREFIX + PROPERTY_CHANNELS, new Integer(encoderFormat.getChannels()));
      receiverConfiguration.setProperty(PREFIX + PROPERTY_QUALITY, new Float(encoderFormat.getQuality()));
      receiverConfiguration.setProperty(PREFIX + PROPERTY_SAMPLERATE, new Integer(encoderFormat.getSampleRate()));
    } else {
      throw new IllegalArgumentException("Unknown receiver : '" + configuration + "'");
    }
  }

  public ReceiverConfiguration load(DataConfiguration receiverConfiguration)
      throws ConfigurationException {
    String receiverClass = receiverConfiguration.getString(PROPERTY_CLASS);

    if (receiverClass.equals(CLASS_SHOUTCLIENT)) {
      ShoutClientReceiverConfiguration configuration = new ShoutClientReceiverConfiguration();
      configuration.setUrl(receiverConfiguration.getURL(PROPERTY_URL));
      return configuration;
    }

    if (receiverClass.equals(CLASS_PEER)) {
      return new PeerReceiverConfiguration();
    }

    if (receiverClass.equals(CLASS_TEST)) {
      return new TestReceiverConfiguration();
    }

    if (receiverClass.equals(CLASS_SHOUTSERVER)) {
      ShoutServerReceiverConfiguration configuration = new ShoutServerReceiverConfiguration();
      configuration.setListenAddress(inetSocketAddressConfigurator
          .load(Configurations.subset(receiverConfiguration, PROPERTY_LISTENADDRESS)));
      return configuration;
    }

    if (receiverClass.equals(CLASS_PLAYLIST)
        || receiverClass.equals(CLASS_PLAYLISTENCODER)) {
      String playlistURIString = receiverConfiguration.getString(PROPERTY_URL);
      URI playlistURI = null;
      try {
        playlistURI = new URI(playlistURIString);
      } catch (URISyntaxException e) {
        throw new ConfigurationException("invalid playlist url: '"
            + playlistURIString, e);
      }

      if (receiverClass.equals(CLASS_PLAYLIST)) {
        PlaylistReceiverConfiguration configuration = new PlaylistReceiverConfiguration();
        configuration.setUri(playlistURI);
        configuration.setBandwidth(receiverConfiguration
            .getInt(PROPERTY_BANDWIDTH, 40));

        LogFactory.getLog(getClass()).warn(
            "the playlist receiver uses a static bandwidth controler at "
                + configuration.getBandwidth());
        return configuration;
      } else {
        PlaylistEncoderReceiverConfiguration configuration = new PlaylistEncoderReceiverConfiguration();

        int channels = receiverConfiguration.getInt(PROPERTY_CHANNELS,
            EncoderFormat.DEFAULT.getChannels());
        int sampleRate = receiverConfiguration.getInt(PROPERTY_SAMPLERATE,
            EncoderFormat.DEFAULT.getSampleRate());
        float quality = receiverConfiguration.getFloat(PROPERTY_QUALITY,
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
