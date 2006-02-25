/*
 * FreeCast - streaming over Internet
 * 
 * This code was developped by Alban Peignier
 * (http://people.tryphon.org/~alban/) and contributors (their names can be
 * found in the CONTRIBUTORS file).
 * 
 * Copyright (C) 2004 Alban Peignier
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.kolaka.freecast.swing;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;

public class IconButton extends JLabel {

	private static final long serialVersionUID = 3257286915873846841L;

	public IconButton(Icon icon) {
		super(icon);
		init();
	}

	public IconButton(Action action) {
		setIcon(getActionIcon(action));
		setAction(action);
		init();
	}

	private void init() {
		setOpaque(false);
		addMouseListener(new MouseAdapter() {
			
			private ActionEventFactory factory = new ActionEventFactory(IconButton.this);
			
			public void mousePressed(MouseEvent event) {
				if (action == null) {
					return;
				}
				if (SwingUtilities.isLeftMouseButton(event)) {
					action.actionPerformed(factory.createActionEvent());
					event.consume();
				}
			}
		});
	}

	private Action action;

	private String iconName = Actions.LARG_ICON;

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getPropertyName().equals(iconName)) {
				Icon icon = (Icon) action.getValue(iconName);
				if (icon != null) {
					setIcon(icon);
					invalidate();
				}
			} else if (event.getPropertyName().equals(Action.SHORT_DESCRIPTION)) {
				setToolTipText((String) action
						.getValue(Action.SHORT_DESCRIPTION));
			}
		}
	};

	public void setAction(final Action action) {
		Validate.notNull(action, "No specified action");

		if (this.action != null) {
			this.action.removePropertyChangeListener(listener);
		}

		setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
		setIcon(getActionIcon(action));
		invalidate();

		action.addPropertyChangeListener(listener);
		this.action = action;
	}

	public void setIcon(Icon icon) {
		if (icon == null) {
			LogFactory.getLog(getClass())
					.debug("no specified icon for " + this);
			return;
		}

		super.setIcon(icon);
		invalidate();
		preferredSize = new Dimension(icon.getIconWidth(), icon.getIconHeight());
	}

	private Icon getActionIcon(Action action) {
		Icon largIcon = Actions.getLargIcon(action);
		if (largIcon != null) {
			iconName = Actions.LARG_ICON;
			return largIcon;
		}
		iconName = Action.SMALL_ICON;
		return Actions.getSmallIcon(action);
	}

	private Dimension preferredSize;

	public Dimension getPreferredSize() {
		if (preferredSize != null) {
			return preferredSize;
		}

		return super.getPreferredSize();
	}

}