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

package org.kolaka.freecast.node;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.player.PlayerStatus;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class NodeStatus implements Serializable {

	static final long serialVersionUID = -7418743416419044755L;

	/**
	 * <strong>Note: </strong> final fields are supported by the Hessian
	 * serialization
	 */
	private NodeIdentifier identifier;

	private Date date;

	private Order order;

	private PlayerStatus playStatus;

	public NodeStatus(NodeIdentifier identifier, Order order) {
		this.identifier = identifier;
		this.date = new Date();
		this.order = order;
	}

	public NodeIdentifier getIdentifier() {
		return identifier;
	}

	public Date getDate() {
		return date;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public Order getOrder() {
		return order;
	}

	public PeerStatus createPeerStatus() {
		return new PeerStatus(identifier, order);
	}

	public PlayerStatus getPlayStatus() {
		return playStatus;
	}

	public void setPlayStatus(PlayerStatus playStatus) {
		this.playStatus = playStatus;
	}
}