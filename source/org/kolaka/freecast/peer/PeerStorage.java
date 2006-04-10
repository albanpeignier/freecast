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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.timer.TimeBase;

public class PeerStorage {

	private final Map peers;

	private final Comparator comparator;

	private long peerTimeout = 2 * DateUtils.MILLIS_PER_MINUTE;

	public PeerStorage(Comparator comparator) {
		this.comparator = comparator;
		this.peers = new TreeMap();
	}

	public List find(Predicate filter) {
		List acceptables = new LinkedList();
		CollectionUtils.select(IteratorUtils.toList(peers()), filter, acceptables);
		Collections.sort(acceptables, comparator);
		return acceptables;
	}
	
	public void trim() {
		long now = timeBase.currentTimeMillis();

		for (Iterator iter = entries(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();

			if (entry.getPeer().getLatency() != Peer.INFINITE_LATENCY) {
				continue;
			}

			long age = entry.getAge(now);
			if (age > peerTimeout) {
				LogFactory.getLog(getClass()).debug("delete peer " + entry);
				iter.remove();
			}
		}
	}
	public void add(MutablePeer peer) {
		peers.put(peer.getStatus().getIdentifier(), new Entry(peer));
	}

	public MutablePeer get(NodeIdentifier identifier) {
		Entry entry = (Entry) peers.get(identifier);

		if (entry == null) {
			return null;
		}

		entry.touch();
		return entry.getPeer();
	}

	public boolean isEmpty() {
		return peers.isEmpty();
	}

	public int size() {
		return peers.size();
	}

	private Iterator entries() {
		return peers.values().iterator();
	}

	public Iterator peers() {
		Collection peers = new ArrayList();

		for (Iterator iter = entries(); iter.hasNext();) {
			Peer peer = ((Entry) iter.next()).getPeer();
			peers.add(peer);
		}

		return peers.iterator();
	}

	public long getPeerTimeout() {
		return peerTimeout;
	}

	public void setPeerTimeout(long peerTimeout) {
		Validate.isTrue(peerTimeout > DateUtils.MILLIS_PER_MINUTE,
				"Peer timeout can't be lower than one minute", peerTimeout);
		this.peerTimeout = peerTimeout;
	}

	private TimeBase timeBase = TimeBase.DEFAULT;

	public void setTimeBase(TimeBase timeBase) {
		Validate.notNull(timeBase, "No specified TimeBase");
		this.timeBase = timeBase;
	}

	public class Entry {

		private long timestamp;

		private final MutablePeer peer;

		Entry(MutablePeer peer) {
			Validate.notNull(peer, "No specified Peer");
			this.peer = peer;

			touch();
		}

		public MutablePeer getPeer() {
			return peer;
		}

		public void touch() {
			timestamp = timeBase.currentTimeMillis();
		}

		public long getAge(long now) {
			return now - timestamp;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}

}