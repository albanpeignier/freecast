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

package org.kolaka.freecast.transport.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.apache.commons.lang.NotImplementedException;

import com.mockobjects.ReturnValue;
import com.mockobjects.util.Verifier;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class MockSocketFactory extends SocketFactory {

	public Socket createSocket(String arg0, int arg1) throws IOException,
			UnknownHostException {
		throw new NotImplementedException(getClass());
	}

	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		throw new NotImplementedException(getClass());
	}

	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		throw new NotImplementedException(getClass());
	}

	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
			int arg3) throws IOException {
		throw new NotImplementedException(getClass());
	}

	private ReturnValue returnSocket = new ReturnValue("socket");

	public Socket createSocket() throws IOException {
		return (Socket) returnSocket.getValue();
	}

	public void setupCreateSocket(Socket socket) {
		returnSocket.setValue(socket);
	}

	public void verify() {
		Verifier.verifyObject(this);
	}

}