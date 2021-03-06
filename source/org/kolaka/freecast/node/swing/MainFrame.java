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

package org.kolaka.freecast.node.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.kolaka.freecast.node.Node;
import org.kolaka.freecast.player.GraphicalPlayerSource;
import org.kolaka.freecast.player.InteractivePlayerSource;
import org.kolaka.freecast.player.PlayerSource;
import org.kolaka.freecast.swing.AsyncAction;
import org.kolaka.freecast.swing.BaseFrame;
import org.kolaka.freecast.swing.ProxyAction;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class MainFrame extends BaseFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2511903707382989163L;

	private InteractivePlayerSource playerSource;
  
	private Action playerAction;

	public MainFrame(Resources resources, Node node) throws ResourcesException {
		super(resources);

		for (Iterator iter = node.getPlayerControler().playerSources()
				.iterator(); iter.hasNext();) {
			PlayerSource source = (PlayerSource) iter.next();
			if (source instanceof InteractivePlayerSource) {
				playerSource = (InteractivePlayerSource) source;
			}
		}

		playerAction = new AsyncAction(new PlayerControlAction(resources, playerSource));
	}
  
  protected JComponent createOptionalPane() throws ResourcesException {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;
    constraints.insets = new Insets(2,2,2,2);
    
    panel.add(new PlayDescriptionPane(playerSource), constraints);
    panel.add(new VolumePane(getResources(), playerSource), constraints);
    
    return panel;    
  }

	protected JComponent createContentPane() {
		if (playerSource instanceof GraphicalPlayerSource) {
			JComponent playerComponent = ((GraphicalPlayerSource) playerSource)
					.getJComponent();
			playerComponent.setBorder(BorderFactory.createLoweredBevelBorder());
      return playerComponent;
		}

		return null;
	}

	protected List createAdditionalActions() {
		ProxyAction proxyPlayerAction = new ProxyAction(playerAction);
		proxyPlayerAction.putValue(Action.SMALL_ICON, null, true);

		return Collections.singletonList(proxyPlayerAction);
	}

	protected Action getButtonAction() {
		return playerAction;
	}

}
