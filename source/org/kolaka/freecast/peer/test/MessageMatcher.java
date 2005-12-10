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

package org.kolaka.freecast.peer.test;

import org.apache.commons.lang.ObjectUtils;
import org.easymock.AbstractMatcher;
import org.kolaka.freecast.transport.Message;
import org.kolaka.freecast.transport.PeerStatusMessage;

class MessageMatcher extends AbstractMatcher {

	private static final PeerStatusMatcher peerStatusMatcher = new PeerStatusMatcher();

	public boolean matches(Object args1[], Object args2[]) {
		Message message1 = (Message) args1[0];
		Message message2 = (Message) args2[0];

		if (message1 instanceof PeerStatusMessage
				&& message2 instanceof PeerStatusMessage) {
			return matches((PeerStatusMessage) message1,
					(PeerStatusMessage) message2);
		}

		return ObjectUtils.equals(message1, message2);
	}

	public boolean matches(PeerStatusMessage message1,
			PeerStatusMessage message2) {
		return peerStatusMatcher.matches(message1.getPeerStatus(), message2
				.getPeerStatus());
	}

}