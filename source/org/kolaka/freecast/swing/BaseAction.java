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
package org.kolaka.freecast.swing;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class BaseAction extends AbstractAction {

	/**
	 * Defines an <code>BaseAction</code> object with a default description
	 * string and default icon.
	 */
	protected BaseAction() {
	}

	/**
	 * Defines an <code>Action</code> object with the specified description
	 * string and a the specified icon.
	 */
	protected BaseAction(String name, Icon icon) {
		super(name, icon);
	}
	
	protected BaseAction(String name) {
		super(name);
		try {
			init();
		} catch (ResourcesException e) {
			LogFactory.getLog(getClass()).error("Can't load action resources", e);
		}
	}
	
	protected void init() throws ResourcesException {
		
	}

	protected Icon loadIcon(String resourceName) {
		URL url = getClass().getResource(resourceName);
		return url != null ? new ImageIcon(url) : null;
	}

	protected void loadIcons(Resources resources, String iconName)
			throws ResourcesException {
		Icon largIcon = null;
		try {
			largIcon = resources.getIcon(iconName + ".larg");
		} catch (ResourcesException e) {
			LogFactory.getLog(getClass()).debug("no larg icon for " + iconName,
					e);
		}
		Icon smallIcon = null;
		try {
			smallIcon = resources.getIcon(iconName + ".small");
		} catch (ResourcesException e) {
			LogFactory.getLog(getClass()).debug(
					"no small icon for " + iconName, e);
		}

		if (largIcon != null && smallIcon != null) {
			Actions.setLargIcon(this, largIcon);
			Actions.setSmallIcon(this, smallIcon);
			return;
		}

		Icon icon;

		if (largIcon != null) {
			icon = largIcon;
		} else if (smallIcon != null) {
			icon = smallIcon;
		} else {
			icon = resources.getIcon(iconName);
		}

		Actions.setSmallIcon(this, icon);
	}

}
