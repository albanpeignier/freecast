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

package org.kolaka.freecast.swing;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class ProxyAction implements Action {

	private final Action action;

	public ProxyAction(Action action) {
		PropertyChangeListener listener = new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				if (!values.containsKey(event.getPropertyName())) {
					support.firePropertyChange(event);
				}
			}

		};
		this.action = action;
		action.addPropertyChangeListener(listener);
	}

	private PropertyChangeSupport support = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	private final Map values = new TreeMap();

	public Object getValue(String key) {
		Object value = values.get(key);
		if (value != null) {
			if (value == ObjectUtils.NULL) {
				return null;
			}

			return value;
		}
		return action.getValue(key);
	}

	public void putValue(String key, Object value) {
		putValue(key, value, false);
	}

	public void putValue(String key, Object value, boolean override) {
		if (!override) {
			action.putValue(key, value);
		} else {
			if (value == null) {
				value = ObjectUtils.NULL;
			}
			values.put(key, value);
		}
	}

	public boolean isEnabled() {
		return action.isEnabled();
	}

	public void setEnabled(boolean b) {
		action.setEnabled(b);
	}

	public void actionPerformed(ActionEvent e) {
		action.actionPerformed(e);
	}

}
