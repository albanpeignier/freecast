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

package org.kolaka.freecast.peer;

import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerConnections {

	public static Predicate acceptStatus(final PeerConnection.Status status) {
		return new Predicate() {

			public boolean evaluate(Object input) {
				PeerConnection connection = (PeerConnection) input;
				return connection.getStatus().equals(status);
			}

		};
	}
	
	public static void closeQuietly(PeerConnection connection) {
		try {
			connection.close();
		} catch (Exception e) {
			LogFactory.getLog(PeerConnections.class).debug("can't close connection " + connection, e);
		}
	}

}