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

package org.kolaka.freecast.peer.test;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.kolaka.freecast.peer.InetPeerReference;
import org.kolaka.freecast.peer.MultiplePeerReference;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.peer.PeerReferenceLoader;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerReferenceLoaderTest extends TestCase {
	private static final int PORT = 1000;

	public void testLoadInetPeerReference() throws ConfigurationException {
		final String host = "host";
		final int port = 4444;

		InetPeerReference referenceWithDefaultPort = (InetPeerReference) loadPeerReference("<host>"
				+ host + "</host>");
		assertEquals(host, referenceWithDefaultPort.getSocketAddress()
				.getHostName());

		InetPeerReference reference = (InetPeerReference) loadPeerReference("<host>"
				+ host + "</host><port>" + port + "</port>");
		assertEquals("host", reference.getSocketAddress().getHostName());
		assertEquals(port, reference.getSocketAddress().getPort());
	}

	public void testMultiplePeerReference() throws ConfigurationException {
		StringBuffer definition = new StringBuffer();
		definition.append("<class>multiple</class>");
		Set expectedReferences = new HashSet();
		final int referenceCount = 5;
		for (int i = 0; i < referenceCount; i++) {
			String hostName = "host" + i;
			definition.append("<reference><host>");
			definition.append(hostName);
			definition.append("</host></reference>");
			expectedReferences.add(InetPeerReference.getInstance(hostName,
					PORT, true));
		}

		MultiplePeerReference reference = (MultiplePeerReference) loadPeerReference(definition
				.toString());
		assertEquals(referenceCount, reference.references().size());

		assertEquals(new MultiplePeerReference(expectedReferences), reference);
	}

	protected PeerReference loadPeerReference(String xmlConfiguration)
			throws ConfigurationException {
		Configuration configuration = createConfiguration("<reference>"
				+ xmlConfiguration + "</reference>");

		PeerReferenceLoader loader = new PeerReferenceLoader();
		loader.setListenAddress(new InetSocketAddress(PORT));

		return loader.load(configuration);
	}

	protected Configuration createConfiguration(String xmlContent)
			throws ConfigurationException {
		XMLConfiguration configuration = new XMLConfiguration();
		configuration.setThrowExceptionOnMissing(true);
		configuration.load(new StringReader(xmlContent));
		return configuration;
	}

}
