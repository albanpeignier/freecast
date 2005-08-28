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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.Validate;
import org.kolaka.freecast.lang.UnexpectedException;
import org.kolaka.freecast.packet.Checksum;
import org.kolaka.freecast.packet.PacketData;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class DigestPacketChecksummer implements PacketChecksummer {

    private final MessageDigest digest;

    public DigestPacketChecksummer(MessageDigest digest) {
        Validate.notNull(digest);
        this.digest = digest;
    }
    
    public Checksum checksum(PacketData packetData) {
        digest.reset();
        digest.update(packetData.getBytes());
        return new Checksum(digest.digest());
    }
    
    public static DigestPacketChecksummer getInstance(String algorithm) throws NoSuchAlgorithmException {
        return new DigestPacketChecksummer(MessageDigest.getInstance(algorithm));
    }
    
    public static DigestPacketChecksummer getInstance() {
        try {
            return getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new UnexpectedException("No SHA Algorithm", e);
        }
    }

}
