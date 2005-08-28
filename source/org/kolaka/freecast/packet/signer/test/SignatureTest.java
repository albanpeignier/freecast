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

package org.kolaka.freecast.packet.signer.test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

import junit.framework.TestCase;

import org.apache.commons.lang.time.StopWatch;

public class SignatureTest extends TestCase {

    public void testSimpleSignature() throws Exception {
        byte[] data = new byte[1024];
        for (int i=0; i < data.length; i++) {
            data[i] = (byte) (Byte.MIN_VALUE + (Math.random() * (Byte.MAX_VALUE - Byte.MIN_VALUE)));
        }
        
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance ("RSA");
        keyPairGenerator.initialize(1024);
        
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        StopWatch watch = new StopWatch();
        watch.start();
        int operationCount = 100;
        for (int i=0; i < operationCount; i++) {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(keyPair.getPrivate());
            
            signature.update(data);
            
            byte signedChecksum[] = signature.sign();
            
            Signature verifierSignature = Signature.getInstance("SHA1withRSA");
            verifierSignature.initVerify(keyPair.getPublic());
            
            verifierSignature.update(data);
            assertTrue(verifierSignature.verify(signedChecksum));
        }
        watch.stop();
        System.out.println("time to sign and verify: " + (watch.getTime() / operationCount) + " ms");
    }
    
}
