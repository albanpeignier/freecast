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

package org.kolaka.freecast.packet.signer;

import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.Signature;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.packet.Checksum;
import org.kolaka.freecast.packet.PacketData;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class SignaturePacketChecksummer implements PacketChecksummer,
		SignaturePacketConstants {

	private final String algorithm;

	private final PrivateKey privateKey;

	public SignaturePacketChecksummer(String algorithm, PrivateKey privateKey) {
		Validate.notNull(algorithm, "No specified algorithm");
		Validate.notNull(privateKey, "No specified PrivateKey");

		this.algorithm = algorithm;
		this.privateKey = privateKey;
	}

	public static SignaturePacketChecksummer getInstance(URL privateKeyURL)
			throws IOException {
		PrivateKey privateKey = (PrivateKey) SerializationUtils
				.deserialize(privateKeyURL.openStream());
		return new SignaturePacketChecksummer(DEFAULT_ALGORITHM, privateKey);
	}

	public Checksum checksum(PacketData packetData)
			throws PacketChecksummerException {
		try {
			Signature signature = Signature.getInstance(algorithm);
			signature.initSign(privateKey);
			signature.update(packetData.getBytes());
			return new Checksum(signature.sign());
		} catch (Exception e) {
			throw new PacketChecksummerException("Can't sign the PacketData "
					+ packetData, e);
		}
	}

	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("algorithm", algorithm);
		return builder.toString();
	}

}
