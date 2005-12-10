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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import com.mockobjects.ExpectationValue;
import com.mockobjects.ReturnValue;
import com.mockobjects.Verifiable;
import com.mockobjects.util.Verifier;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class MockSocket extends Socket implements Verifiable {

	public MockSocket() {

	}

	private ExpectationValue expectedBindpoint = new ExpectationValue(
			"bindpoint");

	public void bind(SocketAddress bindpoint) throws IOException {
		expectedBindpoint.setActual(bindpoint);
	}

	public void setExpectedBindpoint(SocketAddress bindpoint) {
		expectedBindpoint.setExpected(bindpoint);
	}

	private ReturnValue returnedInputStream = new ReturnValue("inputStream");

	private ReturnValue returnedOutputStream = new ReturnValue("outputStream");

	public InputStream getInputStream() throws IOException {
		return (InputStream) returnedInputStream.getValue();
	}

	public OutputStream getOutputStream() throws IOException {
		return (OutputStream) returnedOutputStream.getValue();
	}

	public void setupGetInputStream(InputStream inputStream) {
		returnedInputStream.setValue(inputStream);
	}

	public void setupGetOutputStream(OutputStream outputStream) {
		returnedOutputStream.setValue(outputStream);
	}

	public void verify() {
		Verifier.verifyObject(this);
	}

	private ExpectationValue expectedConnectionPoint = new ExpectationValue(
			"connectionPoint");

	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		expectedConnectionPoint.setActual(endpoint);
	}

	public void setExpectedConnectionPoint(SocketAddress connectionPoint) {
		expectedConnectionPoint.setExpected(connectionPoint);
	}
}