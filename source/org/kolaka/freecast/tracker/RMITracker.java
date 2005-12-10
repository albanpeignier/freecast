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

package org.kolaka.freecast.tracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatus;
import org.kolaka.freecast.peer.PeerReference;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class RMITracker extends UnicastRemoteObject implements RemoteTracker {

	private static final long serialVersionUID = 3763097470581814324L;

	private final Tracker tracker;

	/**
	 * @throws java.rmi.RemoteException
	 */
	public RMITracker(int port) throws RemoteException {
		super(port);

		DefaultTracker.ClientInfoProvider clientInfoProvider = new DefaultTracker.ClientInfoProvider() {
			public String getClientHost() throws TrackerException {
				try {
					return UnicastRemoteObject.getClientHost();
				} catch (ServerNotActiveException e) {
					throw new TrackerException("Unexpected RMI exception", e);
				}
			}
		};

		tracker = new DefaultTracker(clientInfoProvider);
	}

	private static final String DEFAULT_NAME = "RMITracker";

	public void bind(InetSocketAddress address) throws IOException {
		LogFactory.getLog(getClass()).debug(
				"create registry on " + address.getPort());
		Registry registry = LocateRegistry.createRegistry(address.getPort());

		String bindName = getBindName();

		LogFactory.getLog(getClass()).debug("bind tracker as " + bindName);
		try {
			registry.bind(bindName, this);
		} catch (AlreadyBoundException e) {
			throw new IOException("RMITracker already bound at " + address);
		}
	}

	private static String getBindName() {
		return DEFAULT_NAME;
	}

	public static RemoteTracker connect(InetSocketAddress address)
			throws IOException {
		LogFactory.getLog(RMITracker.class).debug(
				"retrieve registry at " + address.getHostName() + ":"
						+ address.getPort());
		Registry registry = LocateRegistry.getRegistry(address.getHostName(),
				address.getPort());

		String bindName = getBindName();
		LogFactory.getLog(RMITracker.class).debug(
				"lookup tracker at " + bindName);
		try {
			RemoteTracker tracker = (RemoteTracker) registry.lookup(bindName);
			LogFactory.getLog(RMITracker.class).debug(
					"retrieved RemoteTracker " + tracker);
			return tracker;
		} catch (NotBoundException e) {
			IOException exception = new IOException("No RMITracker bound at "
					+ address);
			exception.initCause(e);
			throw exception;
		}
	}

	public Set getPeerReferences(NodeIdentifier identifier)
			throws TrackerException {
		return tracker.getPeerReferences(identifier);
	}

	public NodeIdentifier register(PeerReference reference)
			throws TrackerException {
		return tracker.register(reference);
	}

	public void unregister(NodeIdentifier identifier) throws TrackerException {
		tracker.unregister(identifier);
	}

	public void refresh(NodeStatus status) throws TrackerException {
		tracker.refresh(status);
	}

}