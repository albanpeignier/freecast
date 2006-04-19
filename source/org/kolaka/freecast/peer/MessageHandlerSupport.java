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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.MessageHandler;

public class MessageHandlerSupport {

	private MessageHandler handler;
	private List firstMessages = new LinkedList();
	private int cacheSize = 20;

	public void setMessageHandler(MessageHandler handler) {
		Validate.notNull(handler);
		this.handler = handler;
		
		for (Iterator iter = firstMessages.iterator(); iter.hasNext();) {
			Message message = (Message) iter.next();
			LogFactory.getLog(getClass()).trace("handle message received previously " + message);
			handler.messageReceived(message);
		}
	}

	public void processMessage(Message message) {
		if (handler != null) {
			handler.messageReceived(message);
		} else {
			firstMessages.add(message);
			while (firstMessages.size() > cacheSize ) {
				Message garbagedMessage = (Message) firstMessages.remove(0);
				LogFactory.getLog(getClass()).trace("garbage message " + garbagedMessage);
			}
		}
	}

}
