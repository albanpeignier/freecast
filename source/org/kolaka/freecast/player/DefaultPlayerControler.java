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

package org.kolaka.freecast.player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.pipe.Pipe;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Service;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class DefaultPlayerControler implements PlayerControler {

	private Pipe pipe;

	private final Set sources = new HashSet();

	private final Set players = new HashSet();

	public void setPipe(Pipe pipe) {
		Validate.notNull(pipe, "No specified Pipe");
		this.pipe = pipe;
	}

	public void init() throws ControlException {

	}

	public void dispose() throws ControlException {

	}

	public Set playerSources() {
		return sources;
	}

	public void add(PlayerSource source) {
		sources.add(source);
		source.addListener(sourceListener);
	}

	private final PlayerSource.Listener sourceListener = new PlayerSource.Listener() {

		public void playerCreated(Player player) {
			startPlayer(player);
		}

	};

	private final Service.Listener playerListener = new Service.Adapter() {

		public void serviceStopped(Service service) {
			players.remove(service);
		}

	};

	private int playerIdentifier = 1;

	protected void startPlayer(Player player) {
		LogFactory.getLog(getClass()).debug("start new player " + player);
		player.setConsumer(pipe.createConsumer("player-" + playerIdentifier++));

		try {
			player.init();
			player.start();
		} catch (ControlException e) {
			LogFactory.getLog(getClass()).error(
					"Can't start the new player " + player, e);
		}
		player.add(playerListener);
		players.add(player);
	}

	public void start() throws ControlException {
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			PlayerSource source = (PlayerSource) iter.next();
			source.start();
		}
	}

	public void stop() throws ControlException {
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			PlayerSource source = (PlayerSource) iter.next();
			source.stop();
		}
		for (Iterator iter = new HashSet(players).iterator(); iter.hasNext();) {
			Player player = (Player) iter.next();
			try {
				player.dispose();
				player.stop();
			} catch (ControlException e) {
				LogFactory.getLog(getClass()).error(
						"Can't stop the player " + player, e);
			}
		}
	}

	public Set playerStatusSet() {
		Set statusSet = new HashSet();
		for (Iterator iter = players.iterator(); iter.hasNext();) {
			Player player = (Player) iter.next();
			statusSet.add(player.getPlayerStatus());
		}
		return statusSet;
	}

}
