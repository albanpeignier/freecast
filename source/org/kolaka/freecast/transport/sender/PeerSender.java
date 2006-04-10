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

package org.kolaka.freecast.transport.sender;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.packet.LogicalPage;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerSendingConnection;
import org.kolaka.freecast.peer.event.PeerConnectionStatusEvent;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.pipe.Consumer;
import org.kolaka.freecast.pipe.EmptyPipeException;
import org.kolaka.freecast.service.BaseService;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Controlables;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Loop;
import org.kolaka.freecast.timer.LoopInterruptedException;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;
import org.kolaka.freecast.transport.MessageWriter;
import org.kolaka.freecast.transport.PacketMessage;

/**
 * 
 * @navassoc - - - org.kolaka.freecast.peer.PeerConnection
 * @navassoc - - - org.kolaka.freecast.transport.MessageWriter
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerSender extends BaseService implements Sender, TimerUser {

	private Consumer consumer;

	public void setConsumer(Consumer consumer) {
		Validate.notNull(consumer, "No specified Consumer");
		this.consumer = consumer;
	}

	private final PeerSendingConnection connection;

	public PeerSender(PeerSendingConnection connection) {
		this.connection = connection;
	}

	private final PeerConnectionStatusListener listener = new PeerConnectionStatusListener() {
		public void peerConnectionStatusChanged(PeerConnectionStatusEvent event) {
			if (!event.getStatus().equals(PeerConnection.Status.ACTIVATED)) {
				LogFactory.getLog(getClass()).debug("connection disactivated, stop the sender");
				Controlables.stopQuietly(PeerSender.this);
			}
		}
	};

	private Loop sendLoop = new Loop() {

		private MessageWriter writer;

		protected long loop() throws LoopInterruptedException {
			if (writer == null) {
				writer = connection.getWriter();
			}

			LogicalPage page;

			try {
				page = consumer.consume();
			} catch (EmptyPipeException e) {
				return DefaultTimer.seconds(1);
			}

			try {
				for (Iterator iter = page.packets().iterator(); iter.hasNext();) {
					Packet packet = (Packet) iter.next();
					writer.write(PacketMessage.getInstance(packet));
				}
			} catch (IOException e) {
				throw new LoopInterruptedException(
						"failed to send a packet via " + connection, e);
			}

			// return DefaultTimer.nodelay();
			return 100;
		}

	};

	public void start() throws ControlException {
		super.start();

		connection.add(listener);
		timer.execute(sendLoop);
	}

	public void stop() throws ControlException {
		consumer.close();

		connection.remove(listener);
		sendLoop.cancel();

		super.stop();
	}

	private Timer timer = DefaultTimer.getInstance();

	public void setTimer(Timer timer) {
		Validate.notNull(timer, "No specified Timer");
		this.timer = timer;
	}
}