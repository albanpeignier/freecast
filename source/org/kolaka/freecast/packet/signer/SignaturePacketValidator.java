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

package org.kolaka.freecast.packet.signer;

import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.security.Signature;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.Validate;
import org.kolaka.freecast.packet.Packet;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class SignaturePacketValidator implements PacketValidator,
		SignaturePacketConstants {

	private final String algorithm;

	private final PublicKey publicKey;

	public SignaturePacketValidator(String algorithm, PublicKey publicKey) {
		Validate.notNull(algorithm, "No specified algorithm");
		Validate.notNull(publicKey, "No specified PublicKey");

		this.algorithm = algorithm;
		this.publicKey = publicKey;
	}

	public static SignaturePacketValidator getInstance(URL publicKeyURL)
			throws IOException {
		PublicKey publicKey = (PublicKey) SerializationUtils
				.deserialize(publicKeyURL.openStream());
		return new SignaturePacketValidator(DEFAULT_ALGORITHM, publicKey);
	}

	public boolean validate(Packet packet) throws PacketValidatorException {
		try {
			Signature verifierSignature = Signature.getInstance(algorithm);
			verifierSignature.initVerify(publicKey);
			verifierSignature.update(packet.getBytes());
			return verifierSignature.verify(packet.getChecksum().getData());
		} catch (Exception e) {
			throw new PacketValidatorException(
					"Can't verify the packet signature", e);
		}
	}

}
