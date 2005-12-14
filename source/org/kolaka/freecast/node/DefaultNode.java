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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.event.NodeStatusListener;
import org.kolaka.freecast.node.event.NodeStatusSupport;
import org.kolaka.freecast.peer.ConfigurablePeerControler;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerControler;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.pipe.DefaultPipe;
import org.kolaka.freecast.pipe.Pipe;
import org.kolaka.freecast.player.DefaultPlayerControler;
import org.kolaka.freecast.player.PlayerControler;
import org.kolaka.freecast.player.PlayerStatus;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.transport.receiver.NullReceiverControler;
import org.kolaka.freecast.transport.receiver.PeerReceiverControler;
import org.kolaka.freecast.transport.receiver.ReceiverControler;
import org.kolaka.freecast.transport.sender.NullSenderControler;
import org.kolaka.freecast.transport.sender.PeerSenderControler;
import org.kolaka.freecast.transport.sender.SenderControler;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultNode implements ConfigurableNode {

	private PlayerControler playerControler;

	private SenderControler senderControler;

	private ReceiverControler receiverControler;

	private Order order = Order.UNKNOWN;

	private Pipe pipe;

	public DefaultNode() {
		this(new DefaultPipe());
	}

	public DefaultNode(Pipe pipe) {
		this.pipe = pipe;

		senderControler = new NullSenderControler();
		receiverControler = new NullReceiverControler();
		playerControler = new DefaultPlayerControler();
	}

	public void init() throws ControlException {
		LogFactory.getLog(getClass()).debug("init " + this);

		if (peerControler == null) {
			throw new ControlException("No defined PeerControler");
		}

		nodeService.setNode(this);
		nodeService.init();

		if (receiverControler instanceof PeerReceiverControler) {
			((PeerReceiverControler) receiverControler)
					.setPeerControler(peerControler);
		}

		if (senderControler instanceof PeerSenderControler) {
			((PeerSenderControler) senderControler)
					.setPeerControler(peerControler);
		}

		peerControler.addPeerListener(new PeerPropertyChangeListener());

		
		senderControler.setPipe(pipe);

		peerControler.setNodeStatusProvider(nodeStatusProvider);

		receiverControler.init();
		senderControler.init();

		LogFactory.getLog(getClass()).debug("init player");
		playerControler.setPipe(pipe);

		playerControler.init();

		LogFactory.getLog(getClass()).debug("initialized");
	}

	public void start() throws ControlException {
		nodeService.start();
		peerControler.start();

		receiverControler.start();
		senderControler.start();

		LogFactory.getLog(getClass()).debug("start player");
		playerControler.start();
	}

	public void stop() throws ControlException {
		nodeService.stop();
		peerControler.stop();

		receiverControler.stop();
		senderControler.stop();

		playerControler.stop();
	}

	public void dispose() throws ControlException {
		nodeService.dispose();

		receiverControler.dispose();
		senderControler.dispose();

		playerControler.dispose();
	}

	public PlayerControler getPlayerControler() {
		return playerControler;
	}

	public void setPlayerControler(PlayerControler playerControler) {
		this.playerControler = playerControler;
	}

	public ReceiverControler getReceiverControler() {
		return receiverControler;
	}

	public void setReceiverControler(ReceiverControler receiverControler) {
		Validate.notNull(receiverControler, "No specified ReceiverControler");

		if (receiverControler instanceof PeerReceiverControler) {
			order = Order.UNKNOWN;
		} else {
			order = Order.ZERO;
		}

		this.receiverControler = receiverControler;
		receiverControler.setPipe(pipe);
	}

	public SenderControler getSenderControler() {
		return senderControler;
	}

	public void setSenderControler(SenderControler senderControler) {
		this.senderControler = senderControler;
	}

	private NodeIdentifier identifier;

	public void setIdentifier(NodeIdentifier identifier) {
		this.identifier = identifier;
	}

	public NodeIdentifier getIdentifier() {
		return identifier;
	}

	private PeerReference reference;

	public PeerReference getPeerReference() {
		return reference;
	}

	/**
	 * @param reference
	 *            The reference to set.
	 */
	public void setPeerReference(PeerReference reference) {
		this.reference = reference;
	}

	private NodeService nodeService = new NullNodeService();

	/**
	 * @return Returns the nodeService.
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * @param nodeService
	 *            The nodeService to set.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public Order getOrder() {
		return order;
	}

	protected void changeOrder(Order order) {
		LogFactory.getLog(getClass()).debug("set order to " + order);
		this.order = order;
		nodeStatusProvider.fireNodeStatus();
	}

	private final StatusProvider nodeStatusProvider = new StatusProvider();

	class StatusProvider implements NodeStatusProvider {

		private NodeStatusSupport support = new NodeStatusSupport();

		public void add(NodeStatusListener listener) {
			support.add(listener);
		}

		public NodeStatus getNodeStatus() {
			return new NodeStatus(identifier, order);
		}

		public void remove(NodeStatusListener listener) {
			support.remove(listener);
		}

		public void fireNodeStatus() {
			support.fireNodeStatusChange(getNodeStatus());
		}
	}

	public NodeStatus getNodeStatus() {
		LogFactory.getLog(getClass()).trace("pipe: " + pipe);

		NodeStatus status = new NodeStatus(identifier, order);
		status.setPlayStatus(getActivePlayerStatus());
		return status;
	}

	private PlayerStatus getActivePlayerStatus() {
		Set statusSet = playerControler.playerStatusSet();
		for (Iterator iter = statusSet.iterator(); iter.hasNext();) {
			PlayerStatus status = (PlayerStatus) iter.next();

			// TODO use a Player type
			if (!status.equals(PlayerStatus.INACTIVE)) {
				return status;
			}
		}
		return PlayerStatus.INACTIVE;
	}

	public void checkQoS() {
		PlayerStatus status = getActivePlayerStatus();
		if (status.getPlayTimeLength() > 2 * 60000
				&& status.getMissingDataRate() > 0.3) {
			LogFactory.getLog(getClass()).warn(
					"bad audio stream reception (" + status + ")");
			/*
			 * if (receiverControler instanceof PeerReceiverControler) {
			 * ((PeerReceiverControler) receiverControler).resetReceivers(); }
			 */
		}
	}

	private ConfigurablePeerControler peerControler;

	public void setPeerControler(ConfigurablePeerControler controler) {
		this.peerControler = controler;
	}

	public PeerControler getPeerControler() {
		return peerControler;
	}

	public ConfigurablePeerControler getConfigurablePeerControler() {
		return peerControler;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	class PeerPropertyChangeListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			LogFactory.getLog(getClass()).debug("receive " + event);
			Peer peer = (Peer) event.getSource();
			if (peer.isConnected()
					&& peer.getConnection().getType().equals(
							PeerConnection.Type.SOURCE)) {
				changeOrder(peer.getOrder().lower());
			}
		}

	}

}