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
import org.kolaka.freecast.node.NodeIdentifier;
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

	protected void open(IoSession session) {
		Validate.notNull(session);
		Validate.isTrue(session.isConnected(), "session not connected");

		this.session = session;
		setStatus(PeerConnection.Status.OPENING);
		
		add(new PeerConnectionStatusAdapter() {
			protected void connectionOpened(PeerConnection connection) {
				aliveTask = createAliveTask();
				timer.executePeriodically(PING_DELAY, aliveTask, false);
			}
			protected void connectionClosed(PeerConnection connection) {
				if (aliveTask != null) {
					aliveTask.cancel();
				}
			}
		});
	}
	
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
	
	private MessageWriter writer = new MessageWriter() {
		public int write(Message message) throws IOException {
			if (getStatus().equals(PeerConnection.Status.CLOSED)) {
				throw new IOException("write on a closed connection: " + BaseMinaPeerConnection.this);
			}
			
			if (message instanceof IdentifiableMessage) {
				((IdentifiableMessage) message).setSenderIdentifier(getNodeStatusProvider().getNodeIdentifier());
			}
			LogFactory.getLog(getClass()).trace("message sent " + message);

			WriteFuture future = session.write(message);
			/*
			future.join(50);

			if (!future.isWritten()) {
				LogFactory.getLog(getClass()).trace("message not sent ??");
				// throw new IOException("message not sent: " + message);
			}
			*/
			
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
	
	protected abstract Task createAliveTask();
	
	public void close() throws IOException {
		closeImpl();
	}

}

