/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2005 Alban Peignier
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
package org.kolaka.freecast.node.swing;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.player.InteractivePlayer;
import org.kolaka.freecast.player.InteractivePlayerSource;
import org.kolaka.freecast.player.Player;
import org.kolaka.freecast.player.PlayerSource;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Service;
import org.kolaka.freecast.service.Startable;
import org.kolaka.freecast.swing.Actions;
import org.kolaka.freecast.swing.BaseAction;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PlayerControlAction extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7571570213244844002L;

	private InteractivePlayerSource playerSource;

	private Player player;

	final Icon playerStartedIcon, playerStoppedIcon;

	public PlayerControlAction(Resources resources,
			InteractivePlayerSource playerSource) throws ResourcesException {
		playerStartedIcon = resources.getIcon("player.started");
		playerStoppedIcon = resources.getIcon("player.stopped");

		setState(false);
		setEnabled(false);

		if (playerSource == null) {
			return;
		}

		this.playerSource = playerSource;

		final Service.Listener listener = new Service.Adapter() {
			public void serviceStopped(Service service) {
				setState(false);
			}

			public void serviceStarted(Service service) {
				if (service instanceof InteractivePlayer) {
					player = (Player) service;

					setEnabled(true);
					setState(true);
				}
			}
		};

		playerSource.addListener(new PlayerSource.Listener() {
			public void playerCreated(Player player) {
				player.add(listener);
				// the player can be already started
				if (player.getStatus().equals(Startable.Status.STARTED)) {
					listener.serviceStarted(player);
				}
			}
		});
	}

	private boolean playing;

	public void setState(boolean state) {
		playing = state;
		putValue(NAME, (playing ? "Mute" : "Unmute") + " player");
		Actions.setLargIcon(this, playing ? playerStartedIcon
				: playerStoppedIcon);
		putValue(SHORT_DESCRIPTION, "click to " + (playing ? "mute" : "listen"));
	}

	public void changeState(boolean newState) {
		String newStateDescription = newState ? "start" : "stop";
		LogFactory.getLog(getClass()).debug(newStateDescription);

		try {
			if (newState) {
				playerSource.createPlayer();
			} else {
				player.stop();
				player.dispose();
			}
		} catch (ControlException e) {
			LogFactory.getLog(getClass()).error(
					"Can't " + newStateDescription + " the player", e);
		}
	}

	public void actionPerformed(ActionEvent event) {
		changeState(!playing);
	}

}
