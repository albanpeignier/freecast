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

package org.kolaka.freecast.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.header.MessageHeader;

public class StunClient {

	private InetSocketAddress stunServer;
	
	public StunClient(InetSocketAddress stunServer) {
		this.stunServer = stunServer;
	}
	
	public static void setDefaultServer(InetSocketAddress address) {
		Validate.notNull(address);
		defaultAddress = address;
	}
	
	private static InetSocketAddress defaultAddress = new InetSocketAddress("stun.xten.net", 3478);
	private static StunClient defaultInstance;

	public static StunClient getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new StunClient(defaultAddress);
		}
		return defaultInstance;
	}
	
	private final Log logger = LogFactory.getLog(getClass());
	
	public InetSocketAddress getPublicSocketAddress(int localPort) throws IOException {
		int timeSinceFirstTransmission = 0;
		int timeout = 300;
		while (true) {
			DatagramSocket socket = new DatagramSocket(new InetSocketAddress(localPort));
			try {
				socket.setReuseAddress(true);
				socket.connect(stunServer);
				socket.setSoTimeout(timeout);
				
				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				
				ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);
				
				byte[] data = sendMH.getBytes();
				
				logger.debug("send STUN request to " + stunServer + " from " + localPort);
				
				DatagramPacket send = new DatagramPacket(data, data.length, stunServer.getAddress(), stunServer.getPort());
				socket.send(send);
				
				logger.trace("binding request sent");
			
				MessageHeader receiveMH = new MessageHeader();
				while (!(receiveMH.equalTransactionID(sendMH))) {
					DatagramPacket receive = new DatagramPacket(new byte[200], 200);
					socket.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
				}
				
				MappedAddress ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
				ChangedAddress ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
				ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					throw new IOException("Message header contains errorcode message attribute: " + ec.getResponseCode() + " - " + ec.getReason());
				}
				if ((ma == null) || (ca == null)) {
					throw new IOException("Response does not contain a mapped address or changed address message attribute.");
				} else {
					if ((ma.getPort() == socket.getLocalPort()) && (ma.getAddress().getInetAddress().equals(socket.getLocalAddress()))) {
						logger.debug("Node is not natted.");
					} else {
						logger.debug("Node is natted.");
					}
					return new InetSocketAddress(ma.getAddress().getInetAddress(), ma.getPort());
				}
			} catch (SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < 7900) {
					logger.debug("socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = (timeSinceFirstTransmission * 2);
					if (timeoutAddValue > 1600) timeoutAddValue = 1600;
					timeout = timeoutAddValue;
				} else {
					throw new IOException("Node is not capable of udp communication.");
				}
			} catch (Exception e) {
				IOException exception = new IOException("can't process the stun server request");
				exception.initCause(e);
				throw exception;
			} finally {
				socket.close();
			}
		}		
	}
	
}
