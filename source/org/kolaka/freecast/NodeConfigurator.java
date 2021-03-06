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

package org.kolaka.freecast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.config.Configurations;
import org.kolaka.freecast.net.InetSocketAddressSpecification;
import org.kolaka.freecast.net.InetSocketAddressSpecificationParser;
import org.kolaka.freecast.net.SpecificationDatagramSelector;
import org.kolaka.freecast.node.ConfigurableNode;
import org.kolaka.freecast.node.DefaultNodeService;
import org.kolaka.freecast.packet.signer.DigestPacketChecksummer;
import org.kolaka.freecast.packet.signer.DigestPacketValidator;
import org.kolaka.freecast.packet.signer.DummyPacketChecksummer;
import org.kolaka.freecast.packet.signer.DummyPacketValidator;
import org.kolaka.freecast.packet.signer.PacketChecksummer;
import org.kolaka.freecast.packet.signer.PacketChecksummerUser;
import org.kolaka.freecast.packet.signer.PacketValidator;
import org.kolaka.freecast.packet.signer.PacketValidatorUser;
import org.kolaka.freecast.packet.signer.SignaturePacketChecksummer;
import org.kolaka.freecast.packet.signer.SignaturePacketValidator;
import org.kolaka.freecast.peer.ConfigurablePeerControler;
import org.kolaka.freecast.peer.DefaultPeerControler;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.PeerReferenceLoader;
import org.kolaka.freecast.player.AudioPlayerSource;
import org.kolaka.freecast.player.HttpPlayerSource;
import org.kolaka.freecast.player.PlayerSource;
import org.kolaka.freecast.player.VideoPlayerSource;
import org.kolaka.freecast.resource.ResourceLocator;
import org.kolaka.freecast.resource.ResourceLocators;
import org.kolaka.freecast.tracker.NetworkIdentifier;
import org.kolaka.freecast.transport.MinaPeerReceivingConnectionFactory;
import org.kolaka.freecast.transport.MinaPeerSendingConnectionFactory;
import org.kolaka.freecast.transport.cas.ConnectionAssistantClient;
import org.kolaka.freecast.transport.receiver.BandwidthControler;
import org.kolaka.freecast.transport.receiver.EncoderFormat;
import org.kolaka.freecast.transport.receiver.PeerReceiverControler;
import org.kolaka.freecast.transport.receiver.Playlist;
import org.kolaka.freecast.transport.receiver.PlaylistEncoderReceiver;
import org.kolaka.freecast.transport.receiver.PlaylistReceiver;
import org.kolaka.freecast.transport.receiver.Receiver;
import org.kolaka.freecast.transport.receiver.ReceiverControler;
import org.kolaka.freecast.transport.receiver.ResourcePlaylist;
import org.kolaka.freecast.transport.receiver.ShoutClientReceiver;
import org.kolaka.freecast.transport.receiver.ShoutServerReceiver;
import org.kolaka.freecast.transport.receiver.SourceReceiver;
import org.kolaka.freecast.transport.receiver.SourceReceiverControler;
import org.kolaka.freecast.transport.receiver.StaticBandwidthControler;
import org.kolaka.freecast.transport.sender.PeerSenderControler;

/**
 * 
 * @todo make more modular the NodeConfiguration
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class NodeConfigurator {

	private ResourceLocator resourceLocator = ResourceLocators
			.getDefaultInstance();

	public void setResourceLocator(ResourceLocator resourceLocator) {
		Validate.notNull(resourceLocator, "No specified resourceLocator");
		this.resourceLocator = resourceLocator;
	}

	/**
	 * @param node
	 * @param configuration
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public void configure(ConfigurableNode node, Configuration configuration)
			throws ConfigurationException, IOException {
    Configuration peerProviderConfiguration = Configurations.subset(configuration, "peerprovider");
		Configuration trackerAddressConfiguration = Configurations.subset(peerProviderConfiguration, "trackeraddress");
		InetSocketAddress trackerAddress = new InetSocketAddress(
				trackerAddressConfiguration.getString("host"),
				trackerAddressConfiguration.getInt("port"));

		LogFactory.getLog(getClass()).debug(
				"install a NodeService connected to the tracker "
						+ trackerAddress);
		DefaultNodeService nodeService = new DefaultNodeService(trackerAddress);
    
    try {
      nodeService.setNetworkId(NetworkIdentifier.getInstance(peerProviderConfiguration.getString("networkid")));
    } catch (NoSuchElementException e) {
      LogFactory.getLog(getClass()).trace("no network identifier in configuration", e);
    }
    
    node.setNodeService(nodeService);

		ConfigurablePeerControler peerControler = new DefaultPeerControler();
		node.setPeerControler(peerControler);

		// PeerReference and Sender configuration
		PeerReference peerReference = null;
		ConnectionAssistantClient caClient = null;
		
		Configuration casConfiguration = Configurations.subset(configuration, "connection-assistant");
		if (!casConfiguration.isEmpty()) {
			InetSocketAddress casAddress = new InetSocketAddress(
					casConfiguration.getString("host"),
					casConfiguration.getInt("port"));
			caClient = new ConnectionAssistantClient();
			caClient.setServiceAddress(casAddress);
			node.setConnectionAssistantClient(caClient);
		}

		Configuration senderConfiguration = Configurations.subset(configuration, "sender");
		if (!senderConfiguration.isEmpty()) {
			String senderClass = senderConfiguration.getString("class");
			if (senderClass.equals("socket")) {
				LogFactory.getLog(getClass()).warn("socket peer sender is deprecated, the node will use udp transport");
				senderClass = "udp";
			} 
			
			if (senderClass.equals("udp")) {
				Configuration listenAddressConfiguration = Configurations.subset(senderConfiguration, "listenaddress");

				InetSocketAddressSpecification listenAddressSpecification = loadInetSocketAddressSpecification(listenAddressConfiguration);

				InetSocketAddress listenAddress = new SpecificationDatagramSelector()
						.select(listenAddressSpecification);

				LogFactory.getLog(getClass()).debug(
						"install a PeerSenderControler which can accept other peers at "
								+ listenAddress);
				MinaPeerSendingConnectionFactory sendingFactory = new MinaPeerSendingConnectionFactory(listenAddress);
				if (caClient != null) {
					sendingFactory.setConnectionAssistantClient(caClient);
				}
				peerControler.register(sendingFactory);
				sendingFactory.setStatusProvider(node.getNodeStatusProvider());
				
				PeerSenderControler peerSenderControler = new PeerSenderControler(sendingFactory);
				node.setSenderControler(peerSenderControler);

				PeerReferenceLoader peerReferenceLoader = new PeerReferenceLoader();
				peerReferenceLoader.setListenAddress(listenAddress);
				peerReference = peerReferenceLoader.load(Configurations.subset(senderConfiguration,"reference"));
			} else if (senderClass.equals("none")) {
				LogFactory
						.getLog(getClass())
						.warn(
								"no configured sender, your node won't be able to relay the stream");
			} else {
				throw new ConfigurationException("Unknown sender class: '"
						+ senderClass + "'");
			}
		}

		if (peerReference != null) {
			LogFactory.getLog(getClass()).info(
					"set the public reference to " + peerReference);
			node.setPeerReference(peerReference);
			
			if (caClient != null) {
				caClient.setNodeReference(peerReference);
			}
		}

		DataConfiguration receiverConfiguration = new DataConfiguration(
        Configurations.subset(configuration, "receiver"));

		ReceiverControler receiverControler = null;
		Receiver receiver = null;

		String receiverClass = receiverConfiguration.getString("class");
		if (receiverClass.equals("shoutclient")) {
			receiver = new ShoutClientReceiver(receiverConfiguration
					.getURL("url"));
		} else if (receiverClass.equals("playlist")
				|| receiverClass.equals("encoder-playlist")) {
			String playlistURIString = receiverConfiguration.getString("url");
			URI playlistURI = null;
			try {
				playlistURI = new URI(playlistURIString);
			} catch (URISyntaxException e) {
				throw new ConfigurationException("invalid playlist url: '"
						+ playlistURIString, e);
			}

			Playlist playlist = ResourcePlaylist.getInstance(resourceLocator,
					playlistURI);

			if (receiverClass.equals("playlist")) {
				receiver = new PlaylistReceiver(playlist);
				int bandwidth = receiverConfiguration.getInt("bandwidth", 35);
				LogFactory.getLog(getClass()).warn(
						"the playlist receiver uses a static bandwidth controler at "
								+ bandwidth);
				BandwidthControler bandwidthControler = new StaticBandwidthControler(
						(int) (bandwidth * FileUtils.ONE_KB));
				((PlaylistReceiver) receiver)
						.setBandwidthControler(bandwidthControler);
			} else {
				int channels = receiverConfiguration.getInt("channels", 2);
				int sampleRate = receiverConfiguration.getInt("sampleRate",
						44100);
				float quality = receiverConfiguration.getFloat("quality", 0);
				EncoderFormat format = new EncoderFormat(channels, sampleRate,
						quality);
				receiver = new PlaylistEncoderReceiver(playlist, format);
			}
		} else if (receiverClass.equals("shoutserver")) {
			InetSocketAddress listenAddress = loadInetSocketAddress(Configurations.subset(receiverConfiguration
,"listenaddress"));
			receiver = new ShoutServerReceiver(listenAddress);
		} else if (receiverClass.equals("peer")) {
			MinaPeerReceivingConnectionFactory connectionFactory = new MinaPeerReceivingConnectionFactory();
			peerControler.register(connectionFactory);
			connectionFactory.setStatusProvider(node.getNodeStatusProvider());
			if (caClient != null) {
				connectionFactory.setConnectionAssistantClient(caClient);
			}
			receiverControler = new PeerReceiverControler(peerControler, connectionFactory);
		} else {
			throw new ConfigurationException("Unknown receiver class: '"
					+ receiverClass + "'");
		}

		if (receiverControler == null) {
			if (receiver != null) {
				receiverControler = new SourceReceiverControler(
						(SourceReceiver) receiver);
			} else {
				String message = "Miss configuration loading of the Receiver ("
						+ receiverControler + "/" + receiver + ")";
				throw new IllegalStateException(message);
			}
		}

		if (receiverControler instanceof PacketChecksummerUser) {
			DataConfiguration checksummerConfiguration = new DataConfiguration(
          Configurations.subset(receiverConfiguration ,"checksummer"));
			if (!checksummerConfiguration.isEmpty()) {
				PacketChecksummer checksummer;
				String checksummerClass = checksummerConfiguration
						.getString("class");
				if (checksummerClass.equals("digest")) {
					checksummer = DigestPacketChecksummer.getInstance();
				} else if (checksummerClass.equals("signature")) {
					checksummer = SignaturePacketChecksummer
							.getInstance(checksummerConfiguration
									.getURL("privatekey"));
				} else if (checksummerClass.equals("none")) {
					checksummer = new DummyPacketChecksummer();
				} else {
					throw new ConfigurationException(
							"Unknwon checksummer class: '" + checksummerClass
									+ "'");
				}

				LogFactory.getLog(getClass()).debug(
						"install checksummer " + checksummer);
				((PacketChecksummerUser) receiverControler)
						.setPacketChecksummer(checksummer);
			} else {
				LogFactory.getLog(getClass())
						.debug("no checksummer configured");
			}
		} else if (receiverControler instanceof PacketValidatorUser) {
			DataConfiguration validatorConfiguration = new DataConfiguration(
          Configurations.subset(receiverConfiguration, "validator"));
			if (!validatorConfiguration.isEmpty()) {
				PacketValidator validator;
				String validatorClass = validatorConfiguration
						.getString("class");
				if (validatorClass.equals("digest")) {
					validator = new DigestPacketValidator(
							DigestPacketChecksummer.getInstance());
				} else if (validatorClass.equals("signature")) {
					validator = SignaturePacketValidator
							.getInstance(validatorConfiguration
									.getURL("publickey"));
				} else if (validatorClass.equals("none")) {
					validator = new DummyPacketValidator();
				} else {
					throw new ConfigurationException(
							"Unknwon validator class: '" + validatorClass + "'");
				}

				LogFactory.getLog(getClass()).debug(
						"install validator " + validator);
				((PacketValidatorUser) receiverControler)
						.setPacketValidator(validator);
			} else {
				LogFactory.getLog(getClass()).debug("no validator configured");
			}
		} else {
			LogFactory.getLog(getClass()).debug(
					"install no checksummer, no validator");
		}

		LogFactory.getLog(getClass()).debug(
				"install ReceiverControler " + receiverControler);
		node.setReceiverControler(receiverControler);

		Configuration playersConfiguration = Configurations.subset(configuration, "players");

		if (!playersConfiguration.isEmpty()) {
			Set playerClasses = new TreeSet(playersConfiguration
					.getList("player.class"));
			LogFactory.getLog(getClass()).debug("players " + playerClasses);
			for (Iterator iter = playerClasses.iterator(); iter.hasNext();) {
				String playerClass = (String) iter.next();

				PlayerSource playerSource;

				if (playerClass.equals("audio")) {
					playerSource = new AudioPlayerSource(true);
				} else if (playerClass.equals("video")) {
					VideoPlayerSource videoPlayerSource = new VideoPlayerSource(
							true);
					videoPlayerSource.setAudio(playersConfiguration.getBoolean(
							"player.audio", false));
					videoPlayerSource.setFrameRate(playersConfiguration.getInt(
							"player.framerate", 30));
					playerSource = videoPlayerSource;
				} else if (playerClass.equals("http")) {
					InetSocketAddress listenAddress = loadInetSocketAddress(Configurations.subset(playersConfiguration, "player.listenaddress"));
					playerSource = new HttpPlayerSource(listenAddress);
				} else {
					throw new ConfigurationException("Unknwon player class: '"
							+ playerClass + "'");
				}

				LogFactory.getLog(getClass()).debug(
						"install playersource: " + playerSource);
				node.getPlayerControler().add(playerSource);
			}
		} else {
			LogFactory.getLog(getClass()).debug("no configured player");
		}
	}

	private InetSocketAddress loadInetSocketAddress(Configuration configuration) {
		return new InetSocketAddress(
				configuration.getString("host", "0.0.0.0"), configuration
						.getInt("port"));
	}

	private InetSocketAddressSpecification loadInetSocketAddressSpecification(
			Configuration configuration) throws IOException,
			ConfigurationException {
		InetAddress address = InetAddress.getByName(configuration.getString(
				"host", "0.0.0.0"));
		try {
			return new InetSocketAddressSpecificationParser().parse(address,
					configuration.getString("port"));
		} catch (ParseException e) {
			throw new ConfigurationException("Invalid port definition", e);
		}
	}

}
