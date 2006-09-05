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

package org.kolaka.freecast.tracker;

import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.peer.PeerReference;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class ProtectedTracker implements Tracker {

	private final Tracker tracker;

	public ProtectedTracker(final Tracker tracker) {
		this.tracker = tracker;
	}

	public NodeIdentifier register(PeerReference reference)
			throws TrackerException {
		try {
			return tracker.register(reference);
		} catch (TrackerException e) {
			throw e;
		} catch (Exception e) {
			throw createTrackerException(
					"Unexpected exception, unable to register " + reference, e);
		}
	}

	public void unregister(NodeIdentifier identifier) throws TrackerException {
		try {
			tracker.unregister(identifier);
		} catch (TrackerException e) {
			throw e;
		} catch (Exception e) {
			throw createTrackerException(
					"Unexpected exception, unable to unregister " + identifier,
					e);
		}
	}

	public void refresh(NodeStatus status) throws TrackerException {
		try {
			tracker.refresh(status);
		} catch (TrackerException e) {
			throw e;
		} catch (Exception e) {
			throw createTrackerException(
					"Unexpected exception, unable to refresh status" + status,
					e);
		}
	}

	public Set getPeerReferences(NodeIdentifier node) throws TrackerException {
		try {
			return tracker.getPeerReferences(node);
		} catch (TrackerException e) {
			throw e;
		} catch (Exception e) {
			throw createTrackerException(
					"Unexpected exception, unable to retrieve peer references "
							+ node, e);
		}
	}
	
	private TrackerException createTrackerException(String message, Throwable cause) {
		LogFactory.getLog(getClass()).debug("create tracker exception with " + cause, cause);
		return new TrackerException(message, cause);
	}

}
