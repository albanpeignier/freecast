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

package org.kolaka.freecast.node;

import java.net.InetSocketAddress;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.peer.PeerProvider;
import org.kolaka.freecast.peer.PeerProviderException;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.LoopService;
import org.kolaka.freecast.tracker.HttpTrackerLocator;
import org.kolaka.freecast.tracker.Tracker;
import org.kolaka.freecast.tracker.TrackerException;
import org.kolaka.freecast.tracker.TrackerLocator;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultNodeService extends LoopService implements NodeService {

	private ConfigurableNode node;

	private InetSocketAddress trackerAddress;

	/**
	 * 
	 */
protected Loop createLoop() {
		return new Loop() {
			private int loopCount = 0;

			public long loop() throws LoopInterruptedException {
				node.checkQoS();
				loopCount++;

				if (loopCount % 2 == 0) {
					NodeStatus status = node.getNodeStatus();
					LogFactory.getLog(getClass()).debug(
							"refresh node status " + status);

					try {
						try {
							tracker.refresh(status);
						} catch (TrackerException.UnknownNode e) {
							LogFactory.getLog(getClass()).debug(
									"the tracker forgot our existence, register again");
							registerNode();
						} 
					} catch (Throwable t) {
						LogFactory.getLog(getClass()).error(
								"failed to refresh status", t);
					}
				}

				return 30 * 1000;
			}
		};
	}
	private TrackerLocator trackerLocator = HttpTrackerLocator.getInstance();

	private Tracker tracker;

	/**
	 * 
	 */
	public void init() throws ControlException {
		try {
			tracker = trackerLocator.resolve(trackerAddress);
		} catch (TrackerException e) {
			throw new ControlException("Can't connect to the tracker "
					+ trackerAddress, e);
		}

		super.init();
	}

	/**
	 * @param node
	 *            The node to set.
	 */
	public void setNode(ConfigurableNode node) {
		this.node = node;
	}

	/**
	 * @param trackerAddress
	 *            The trackerAddress to set.
	 */
	public void setTrackerAddress(InetSocketAddress trackerAddress) {
		this.trackerAddress = trackerAddress;
	}

	/**
	 * 
	 */

	public void start() throws ControlException {
		try {
			registerNode();
		} catch (TrackerException e) {
			throw new ControlException("Can't connect to the tracker "
					+ trackerAddress, e);
		}

		// TODO move the PeerProvider setting (to allow
		// MinimumOrderPeerProvider)
		LogFactory.getLog(getClass()).debug(
				"change the peer provider to use tracker");
		node.getConfigurablePeerControler().setPeerProvider(
				createPeerProvider());

		super.start();
	}

	/**
	 * @throws TrackerException
	 */
	private void registerNode() throws TrackerException {
		NodeIdentifier identifier = tracker.register(node.getPeerReference());
		LogFactory.getLog(getClass()).info(
				"received node identifier " + identifier);
		node.setIdentifier(identifier);
	}

	protected PeerProvider createPeerProvider() {
		return new PeerProvider() {
			public Set getPeerReferences() throws PeerProviderException {
				try {
					return tracker.getPeerReferences(node.getIdentifier());
				} catch (TrackerException e) {
					String msg = "Can't retrieve new peer references from the tracker";
					throw new PeerProviderException(msg, e);
				}
			}
		};
	}

	/**
	 * @param trackerAddress
	 */
	public DefaultNodeService(InetSocketAddress trackerAddress) {
		this.trackerAddress = trackerAddress;
	}

	public void stop() throws ControlException {
		super.stop();

		LogFactory.getLog(getClass()).info("unregister node");

		if (tracker != null) {
			try {
				tracker.unregister(node.getIdentifier());
			} catch (TrackerException e) {
				throw new ControlException("Can't unregister the node", e);
			}
		}
	}
}