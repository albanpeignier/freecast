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

package org.kolaka.freecast.node.test;

import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.node.Order;
import org.kolaka.freecast.node.event.NodeStatusListener;

public class MockNodeStatusProvider implements NodeStatusProvider {

	private NodeIdentifier identifier;
	private final NodeStatus status;
	
	public MockNodeStatusProvider() {
		this(new NodeStatus(new NodeIdentifier(0), Order.ZERO));
	}
	
	public MockNodeStatusProvider(NodeStatus status) {
		this.status = status;
		this.identifier = status.getIdentifier();
	}

	public NodeIdentifier getNodeIdentifier() {
		return identifier;
	}
	
	public NodeStatus getNodeStatus() {
		return status;
	}

	public void add(NodeStatusListener listener) {

	}

	public void remove(NodeStatusListener listener) {

	}

}
