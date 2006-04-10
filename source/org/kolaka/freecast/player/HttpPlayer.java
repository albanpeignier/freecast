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

package org.kolaka.freecast.player;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.pipe.Consumer;
import org.kolaka.freecast.pipe.ConsumerInputStreamFactory;
import org.kolaka.freecast.service.BaseService;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class HttpPlayer extends BaseService implements Player, TimerUser {

	private Socket socket;

	private Consumer consumer;

	private boolean stopped;

	private ConsumerInputStreamFactory inputFactory;

	public HttpPlayer(Socket socket) {
		Validate.notNull(socket, "No specified socket");
		this.socket = socket;
	}

	private Task sending = new Task() {

		public void run() {
			OutputStream output = null;

			try {
				ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

				while (true) {
					int read = socket.getInputStream().read();
					if (read == -1) {
						throw new EOFException("incomplete header");
					}

					if (read == '\n') {
						String headerLine = new String(lineBuffer.toByteArray())
								.trim();
						lineBuffer.reset();

						if (headerLine.length() == 0) {
							LogFactory.getLog(getClass()).trace("header ended");
							break;
						}
						LogFactory.getLog(getClass()).trace(
								"received header: " + headerLine);
					} else {
						lineBuffer.write(read);
					}
				}

				// socket.setSendBufferSize(1024);
				output = socket.getOutputStream();

				String reply = "HTTP/1.0 200 OK\r\n"
						+ "Content-Type: application/ogg\r\nice-name: FreeCast\r\n\r\n";

				output.write(reply.getBytes());
				output.flush();

				while (!stopped) {
					InputStream input = inputFactory.next();
					CopyUtils.copy(input, output);
					IOUtils.closeQuietly(input);
				}
			} catch (IOException e) {
				LogFactory.getLog(getClass()).info(
						"connection with http player ended", e);
			} catch (Exception e) {
				LogFactory.getLog(getClass()).error(
						"error in the http player connection", e);
			} finally {
				stopImpl();
			}
		}

	};

	protected void stopImpl() {
		stopped = true;

		LogFactory.getLog(getClass()).debug("dispose http player resources");

		try {
			socket.close();
		} catch (IOException e) {
			LogFactory.getLog(getClass()).error(
					"can't close the http player socket", e);
		}

		if (inputFactory != null) {
			inputFactory.close();
		}

		try {
			super.stop();
		} catch (ControlException e) {
			LogFactory.getLog(getClass()).error("can't stop this http player",
					e);
		}
	}

	public void start() throws ControlException {
		stopped = false;

		inputFactory = new ConsumerInputStreamFactory(consumer);

		timer.executeLater(sending);

		super.start();
	}

	public void stop() throws ControlException {
		sending.cancel();
		stopImpl();
	}

	public void init() throws ControlException {
		super.init();
	}

	public void dispose() throws ControlException {
		super.dispose();
	}

	public PlayerStatus getPlayerStatus() {
		return PlayerStatus.INACTIVE;
	}

	public void setConsumer(Consumer consumer) {
		this.consumer = consumer;
	}

	private Timer timer = DefaultTimer.getInstance();

	public void setTimer(Timer timer) {
		Validate.notNull(timer, "No specified Timer");
		this.timer = timer;
	}

}
