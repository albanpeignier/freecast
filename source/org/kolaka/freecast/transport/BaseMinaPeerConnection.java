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

package org.kolaka.freecast.transport;

import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.kolaka.freecast.peer.BasePeerConnection2;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.event.PeerConnectionStatusAdapter;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.Timer;

public abstract class BaseMinaPeerConnection extends BasePeerConnection2 {
	
	private IoSession session;
	private Task aliveTask;
	public static final long PING_DELAY =  DefaultTimer.seconds(10);
	public static final long OPEN_TIMEOUT =  DefaultTimer.seconds(10);

	protected void open(IoSession session) {
		Validate.notNull(session);
		Validate.isTrue(session.isConnected(), "session not connected");

		this.session = session;
		setStatus(PeerConnection.Status.OPENING);
		
		add(new PeerConnectionStatusAdapter() {
			protected void connectionOpened(PeerConnection connection) {
				openTimeout.cancel();
				aliveTask = createAliveTask();
				timer.executePeriodically(PING_DELAY, aliveTask, false);
				timer.executePeriodically(AcknowledgmentProcessor.ACKNOWLEDGMENT_DELAY / 3, ackTask, false);
				timer.executePeriodically(AcknowledgmentProcessor.ACKNOWLEDGMENT_DELAY, ackResentTask, false);
			}
			protected void connectionClosed(PeerConnection connection) {
				if (aliveTask != null) {
					ackTask.cancel();
					ackResentTask.cancel();
					aliveTask.cancel();
				}
				openTimeout.cancel();
			}
		});
		
		timer.executeAfterDelay(OPEN_TIMEOUT, openTimeout);
	}
	
	private final Task openTimeout = new Task() {
		public void run() {
			if (getStatus().equals(PeerConnection.Status.OPENING)) {
				LogFactory.getLog(getClass()).debug("open timeout, close connection");
				closeImpl();
			}
		}
	};
	
	protected void closeImpl() {
		try {
			sendConnectionStatus(PeerConnection.Status.CLOSED);
		} catch (IOException e) {
			LogFactory.getLog(getClass()).error("Can't send close message");
		}
		setStatus(PeerConnection.Status.CLOSED);
		session.close();
	}
	
	protected IoSession getSession() {
		return session;
	}
	
	private AcknowledgmentProcessor ackProcessor = new AcknowledgmentProcessor();
	
	protected void processMessage(Message message) throws IOException {
		super.processMessage(message);
		ackProcessor.messageReceived(message);
	}
	
	private Task ackResentTask = new Task() {
		public void run() {
			try {
				ackProcessor.resend(writer);
			} catch (IOException e) {
				LogFactory.getLog(getClass()).error("can't resend some of unacknowledged messages", e);
			}
		}
	};

	private Task ackTask = new Task() {
		public void run() {
			try {
				ackProcessor.acknowledge(writer);
			} catch (IOException e) {
				LogFactory.getLog(getClass()).error("can't acknowledge messages", e);
			}
		}
	};

	private MessageWriter writer = new MessageWriter() {
		public int write(Message message) throws IOException {
			if (getStatus().equals(PeerConnection.Status.CLOSED)) {
				throw new IOException("write on a closed connection: " + BaseMinaPeerConnection.this);
			}
			
			if (message instanceof IdentifiableMessage) {
				((IdentifiableMessage) message).setSenderIdentifier(getNodeStatusProvider().getNodeIdentifier());
			}
			
			LogFactory.getLog(getClass()).trace("message sent " + message + " to " + session.getRemoteAddress());

			WriteFuture future = session.write(message);
			// future.join();
			ackProcessor.messageSent(message);
			
			// TODO fake length, to be removed
			return 1000;
		}
	};

	public MessageWriter getWriter() {
		return writer;
	}
	
	private Timer timer = DefaultTimer.getInstance();
	
	public void setTimer(Timer timer) {
		Validate.notNull(timer);
		this.timer = timer;
	}
	
	protected Timer getTimer() {
		return timer;
	}
	
	protected abstract Task createAliveTask();
	
	public void close() throws IOException {
		if (session != null) {
			session.close();
		}
		closeImpl();
	}

}

