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

package org.kolaka.freecast.transport.receiver;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.OggSocketSource;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Startable;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class ShoutServerOggSourceFactory implements OggSourceFactory, Startable {

	private final InetSocketAddress listenAddress;

	private ServerSocket serverSocket;

	public ShoutServerOggSourceFactory(InetSocketAddress listenAddress) {
		this.listenAddress = listenAddress;
	}

	public OggSource next() throws IOException {
		LogFactory.getLog(getClass()).info(
				"waiting ices connection on " + listenAddress);

		Socket socket = serverSocket.accept();
		LogFactory.getLog(getClass()).info(
				"new ices connection from " + socket.getInetAddress());
		LogFactory.getLog(getClass()).debug("read http header");

		receiveHeader(socket);

		return new OggSocketSource(socket);
	}

	protected void receiveHeader(Socket socket) throws IOException {
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();

		ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

		while (true) {
			int read = input.read();
			if (read == -1) {
				throw new EOFException("incomplete header");
			}

			if (read == '\n') {
				String headerLine = new String(lineBuffer.toByteArray()).trim();
				lineBuffer.reset();

				if (headerLine.length() == 0) {
					LogFactory.getLog(getClass()).trace("header ended");
					output.write("HTTP/1.0 200 OK\r\n\r\n".getBytes());
					output.flush();

					return;
				}
				LogFactory.getLog(getClass()).trace(
						"received header: " + headerLine);
			} else {
				lineBuffer.write(read);
			}
		}
	}

	public void start() throws ControlException {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(listenAddress);
		} catch (IOException e) {
			throw new ControlException("Can't bind the server socket on "
					+ listenAddress, e);
		}
	}

	public void stop() throws ControlException {
		try {
			serverSocket.close();
		} catch (IOException e) {
			LogFactory.getLog(getClass()).error(
					"Can't close the server socket", e);
		}
	}

}
