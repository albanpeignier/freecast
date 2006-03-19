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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.LogFactory;

public class AcknowledgmentProcessor {
	
	private final Map messages = new TreeMap();
	private AcknowledgmentMessage nextAcknowledgmentMessage = new AcknowledgmentMessage();
	
	public static final long ACKNOWLEDGMENT_DELAY = 3000;

	public synchronized void messageSent(Message message) {
		if (!AcknowledgableMessages.requiresAcknowledgment(message)) {
			return;
		}
		
		AcknowledgableMessage acknowledgableMessage = (AcknowledgableMessage) message;
		Integer identifier = new Integer(acknowledgableMessage.getAcknowledgmentIdentifier());
		if (!messages.containsKey(identifier)) {
			LogFactory.getLog(getClass()).trace("add " + identifier);
			messages.put(identifier, new Entry(acknowledgableMessage));
		}
	}
	
	public synchronized void messageReceived(Message message) {
		if (AcknowledgableMessages.requiresAcknowledgment(message)) {
			nextAcknowledgmentMessage.add((AcknowledgableMessage) message);
		}

		if (message instanceof AcknowledgmentMessage) {
			AcknowledgmentMessage acknowledgmentMessage = (AcknowledgmentMessage) message;
			int ackCount = 0;
			for (Iterator iter = acknowledgmentMessage.identifiers(); iter.hasNext();) {
				Integer ackIdentifier = (Integer) iter.next();
				LogFactory.getLog(getClass()).trace("remove " + ackIdentifier);
				messages.remove(ackIdentifier);
				ackCount++;
			}
			LogFactory.getLog(getClass()).trace("acknowledgment received for " + ackCount + " messages");
		}
	}
	
	public void acknowledge(MessageWriter writer) throws IOException {
		if (!nextAcknowledgmentMessage.isEmpty()) {
			LogFactory.getLog(getClass()).trace("send acknowledgment message");
			LogFactory.getLog(getClass()).trace("send acknowledgment message: " + nextAcknowledgmentMessage);
			writer.write(nextAcknowledgmentMessage);
			nextAcknowledgmentMessage = new AcknowledgmentMessage();
		}
	}
	
	public void resend(MessageWriter writer) throws IOException {
		Collection entries;
		
		synchronized (this) {
			entries = new LinkedList(messages.values());	
		}
		
		int resendCount = 0;
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();
			if (entry.requiresResend()) {
				AcknowledgableMessage message = entry.getMessage();
				LogFactory.getLog(getClass()).trace("resend " + message);
				
				resendCount++;
				writer.write(message);
				
				entry.reset();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new IOException("Can't pause the thread");
			}
		}
		
		if (resendCount > 0) {
			LogFactory.getLog(getClass()).debug("resent " + resendCount + " messages");
			LogFactory.getLog(getClass()).trace("unacknowledged Messages: " + messages.size());
		}
	}
	
	class Entry {
		
		private AcknowledgableMessage message;
		private long timestamp;
		
		public Entry(AcknowledgableMessage message) {
			this.message = message;
			reset();
		}
		
		public void reset() {
			this.timestamp = System.currentTimeMillis();
		}

		public AcknowledgableMessage getMessage() {
			return message;
		}

		public boolean requiresResend() {
			return System.currentTimeMillis() - timestamp > ACKNOWLEDGMENT_DELAY;
		}
		
	}
	
	
}
