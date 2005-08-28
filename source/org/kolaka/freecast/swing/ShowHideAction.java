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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ShowHideAction extends BaseAction {

	private final Component component;
	private final String componentName;

	public ShowHideAction(Component component, String componentName) {
		super(createName(component.isVisible(), componentName));
		this.component = component;
		this.componentName = componentName;

		ComponentListener listener = new ShowHideListener() {
			protected void componentVisible(boolean visible) {
				String name = createName();
				putValue(NAME, name);
			}
		};
		component.addComponentListener(listener);
	}

	public ShowHideAction(Resources resources, Component component, String componentName, String iconName) throws ResourcesException {
		this(component, componentName);
		loadIcons(resources, iconName);
	}

	public void actionPerformed(ActionEvent e) {
		component.setVisible(!component.isVisible());
	}

	private String createName() {
		return createName(component.isVisible(), componentName);
	}

	private static String createName(boolean isVisible, String componentName) {
		return (isVisible ? "Hide" : "Show") + " " + componentName;
	}

}
