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

package org.kolaka.freecast.peer;

import java.io.EOFException;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.PeerConnectionStatusSupport;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Loop;
import org.kolaka.freecast.timer.LoopInterruptedException;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.TimeBase;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageReader;
import org.kolaka.freecast.transport.MessageWriter;
import org.kolaka.freecast.transport.PeerConnectionStatusMessage;
import org.kolaka.freecast.transport.PeerStatusMessage;
import org.kolaka.freecast.transport.ProxyMessageReader;
import org.kolaka.freecast.transport.ProxyMessageWriter;

public abstract class BasePeerConnection1 extends BasePeerConnection implements PeerConnection1 {

	private Peer peer;

	private PeerStatus lastPeerStatus;

	// private PeerReference peerReference;
	private final PeerConnection1.Type type;

	private MessageWriter writer;

	private MessageReader reader;

	protected BasePeerConnection1(PeerConnection1.Type type) {
		this.type = type;
	}

	public PeerConnection1.Type getType() {
		return type;
	}

	/**
	 * Sets the specified <code>Status</code> and sends it to the connected
	 * peer.
	 * 
	 * @param status
	 */
	protected void changeStatus(PeerConnection.Status status) {
		Validate.notNull(status, "No specified Status");

		setStatus(status);
		sendStatus(status);
	}

	private void sendStatus(PeerConnection.Status status) {
		SendStatusTask sendStatus = new SendStatusTask(status);
		timer.executeLater(sendStatus);

		if (!sendStatus.confirmMessage()) {
			// TODO manage the status sending errors
			LogFactory.getLog(getClass()).warn(
					"status sending isn't confirmed: " + status);
		} else {
			LogFactory.getLog(getClass()).debug(
					"status sending is confirmed: " + status);
		}
	}

	private Timer timer = DefaultTimer.getInstance();

	public void setTimer(Timer timer) {
		Validate.notNull(timer, "Not specified Timer");
		this.timer = timer;
	}
	
	public PeerStatus getRemoteStatus() {
		return getLastPeerStatus();
	}

	public PeerStatus getLastPeerStatus() {
		if (peer != null) {
			return peer.getStatus();
		}

		if (lastPeerStatus == null) {
			throw new IllegalStateException("No last PeerStatus known");
		}

		return lastPeerStatus;
	}

	private long lastPeerStatusUpdate;

	private void setLastPeerStatus(PeerStatus status) {
		lastPeerStatusUpdate = timeBase.currentTimeMillis();
		if (peer != null) {
			((MutablePeer) peer).update(status);
		} else {
			lastPeerStatus = status;
		}
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public Peer getPeer() {
		return peer;
	}

	/*
	 * public PeerReference getPeerReference() { return peerReference; }
	 */
	public MessageReader getReader() {
		if (reader == null) {
			reader = new FilterReader(getInternalReader());
		}
		return reader;
	}

	public MessageWriter getWriter() {
		if (writer == null) {
			writer = new FilterWriter(getInternalWriter());
		}
		return writer;
	}

	private MessageWriter internalWriter;

	private MessageWriter getInternalWriter() {
		if (internalWriter == null) {
			internalWriter = createWriter();
		}
		return internalWriter;
	}

	private MessageReader internalReader;

	private MessageReader getInternalReader() {
		if (internalReader == null) {
			internalReader = createReader();
		}
		return internalReader;
	}

	protected abstract MessageReader createReader();

	protected abstract MessageWriter createWriter();

	/**
	 * Invoked by the <code>PeerConnectionSource</code> when the opening
	 * <code>PeerConnection</code> is accepting.
	 */
	public void open() {
		changeStatus(PeerConnection.Status.OPENED);

		/*
		 * When the connection is a Type.RELAY, start a loop to read the control
		 * messages
		 */
		if (type.equals(PeerConnection1.Type.RELAY)) {
			receiveLoop = new ReceiveLoop();
			timer.execute(receiveLoop);

			checkReceivedStatusTask = new CheckReceivedStatusTask();
			timer.executePeriodically(DefaultTimer.seconds(30),
					checkReceivedStatusTask, false);
		}
	}

	public void close() {
		LogFactory.getLog(getClass()).debug("close");
		if (getStatus().equals(PeerConnection.Status.CLOSING) || getStatus().equals(PeerConnection.Status.CLOSED)) {
			return;
		}

		// TODO asynchronous status sending can be problematic
		changeStatus(PeerConnection.Status.CLOSED);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			LogFactory.getLog(getClass()).error(
					"Can't wait the status sending", e);
		}

		dispose();
	}

	protected void dispose() {
		LogFactory.getLog(getClass()).debug("dispose");
		setStatus(PeerConnection.Status.CLOSED);

		if (receiveLoop != null) {
			receiveLoop.cancel();
		}

		if (checkReceivedStatusTask != null) {
			checkReceivedStatusTask.cancel();
		}

		try {
			disposeImpl();
		} catch (PeerConnectionException e) {
			LogFactory.getLog(getClass()).error(
					"Can't be disposed properly " + this, e);
		}
	}

	private void closeOnError(IOException e) {
		LogFactory.getLog(getClass()).error("connection closed on error", e);
		dispose();
	}

	protected abstract void disposeImpl() throws PeerConnectionException;

	class FilterWriter extends ProxyMessageWriter {

		FilterWriter(MessageWriter writer) {
			super(writer);
		}

		public int write(Message message) throws IOException {
			if (getStatus().equals(PeerConnection.Status.CLOSED)) {
				throw new NotOpenConnectionException(getStatus());
			}

			try {
				return super.write(message);
			} catch (IOException e) {
				closeOnError(e);
				throw e;
			}
		}

	}

	class FilterReader extends ProxyMessageReader {

		private void checkClosed() throws EOFException {
			if (getStatus().equals(PeerConnection.Status.CLOSED)) {
				throw new EOFException("connection is closed");
			}
		}

		public Message read() throws IOException {
			checkClosed();

			Message message;

			try {
				message = super.read();
			} catch (IOException e) {
				checkClosed();

				closeOnError(e);
				throw e;
			}

			if (message instanceof PeerStatusMessage) {
				PeerStatusMessage peerStatusMessage = (PeerStatusMessage) message;
				PeerStatus status = peerStatusMessage.getPeerStatus();
				LogFactory.getLog(getClass()).debug(
						"receive new peer status: " + status);
				setLastPeerStatus(status);
			}

			if (message instanceof PeerConnectionStatusMessage) {
				PeerConnection.Status status = ((PeerConnectionStatusMessage) message)
						.getStatus();
				LogFactory.getLog(getClass()).debug(
						"receive new connection status: " + status);
				if (status.equals(PeerConnection.Status.CLOSED)) {
					dispose();
				} else {
					setStatus(status);
				}
			}

			return message;
		}

		FilterReader(final MessageReader reader) {
			super(reader);
		}

	}

	class SendStatusTask extends Task {

		private final PeerConnection.Status status;

		private final Object lock = new Object();

		private boolean messageSent;

		public void run() {
			NDC.push(BasePeerConnection1.this.toString());

			try {
				LogFactory.getLog(getClass()).debug("send new " + status);

				try {
					getInternalWriter().write(
							new PeerConnectionStatusMessage(status));
					messageSent = true;
				} catch (IOException e) {
					String msg = "Can't send new status " + status;
					if (status.equals(PeerConnection.Status.CLOSED)) {
						// when the transport layer can be already stopped
						LogFactory.getLog(getClass()).warn(msg, e);
					} else {
						LogFactory.getLog(getClass()).error(msg, e);
					}
				}
			} finally {
				NDC.pop();
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		}

		public SendStatusTask(PeerConnection.Status status) {
			this.status = status;
		}

		/**
		 * @todo use the dedicated util.concurrent tool
		 * @return
		 */
		public boolean confirmMessage() {
			synchronized (lock) {
				try {
					lock.wait(DefaultTimer.seconds(2));
				} catch (InterruptedException e) {
					LogFactory.getLog(getClass()).error(
							"Can't wait the message confirmation", e);
					return false;
				}
			}

			return messageSent;
		}

	}

	/**
	 * 
	 */
	public void activate() {
		changeStatus(PeerConnection.Status.ACTIVATED);
	}

	private TimeBase timeBase = TimeBase.DEFAULT;

	private Loop receiveLoop;

	private Task checkReceivedStatusTask;

	/**
	 * Used to reveice control messages when the Reader is not used (like the
	 * relay connections)
	 * 
	 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
	 */
	class ReceiveLoop extends Loop {
		protected long loop() throws LoopInterruptedException {
			Message message;

			try {
				message = getReader().read();
			} catch (EOFException e) {
				if (getStatus().equals(PeerConnection.Status.CLOSED)) {
					LogFactory.getLog(getClass()).debug(
							"reader stopped for " + this, e);
					return DefaultTimer.seconds(2);
				}

				throw new LoopInterruptedException(
						"connection read stream ended", e);
			} catch (IOException e) {
				if (getStatus().equals(PeerConnection.Status.CLOSED)) {
					LogFactory.getLog(getClass()).debug(
							"reader stopped for " + this, e);
				} else {
					LogFactory.getLog(getClass()).error(
							"failed to receive a message via " + this, e);
				}
				return DefaultTimer.seconds(2);
			}

			LogFactory.getLog(getClass()).trace("receive " + message);

			return DefaultTimer.nodelay();
		}
	}

	class CheckReceivedStatusTask extends Task {

		public void run() {
			long now = timeBase.currentTimeMillis();
			if (now - lastPeerStatusUpdate > DefaultTimer.minutes(1)) {
				String msg = "status update timeout on " + BasePeerConnection1.this
						+ ", connection is disposed";
				LogFactory.getLog(getClass()).info(msg);
				dispose();
			} else {
				String msg = "update timeout valid (last update receiving at "
						+ new Date(lastPeerStatusUpdate);
				LogFactory.getLog(getClass()).trace(msg);
			}
		}

	}

}