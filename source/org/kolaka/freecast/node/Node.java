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

import org.kolaka.freecast.node.DefaultNode.StatusProvider;
import org.kolaka.freecast.peer.PeerControler;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.player.PlayerControler;
import org.kolaka.freecast.service.Controlable;
import org.kolaka.freecast.transport.receiver.ReceiverControler;
import org.kolaka.freecast.transport.sender.SenderControler;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public interface Node extends Controlable {

	public PeerReference getPeerReference();

	public NodeIdentifier getIdentifier();

	public Order getOrder();

	public ReceiverControler getReceiverControler();

	public SenderControler getSenderControler();

	public NodeService getNodeService();

	public PeerControler getPeerControler();

	public NodeStatus getNodeStatus();
	
	StatusProvider getNodeStatusProvider();

	public void checkQoS();

	public PlayerControler getPlayerControler();

}