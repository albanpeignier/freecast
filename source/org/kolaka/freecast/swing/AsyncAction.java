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

package org.kolaka.freecast.swing;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class AsyncAction implements Action {

  private final Action delegate;

	public AsyncAction(Action delegate) {
		this.delegate = delegate;
	}
	
	public Object getValue(String key) {
		return delegate.getValue(key);
	}

	public void putValue(String key, Object value) { 
		delegate.putValue(key, value);
	}

	public void setEnabled(boolean b) {
		delegate.setEnabled(b);
	}
	
 	public boolean isEnabled() {
 		return delegate.isEnabled();
 	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		delegate.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		delegate.removePropertyChangeListener(listener);
	}
	
	public void actionPerformed(final ActionEvent event) {
		LogFactory.getLog(getClass()).debug("execute " + delegate + " with " + event);
	
		// TODO use a Thread Queue (like freecast.timer.Timer)
		Runnable runnable = new Runnable() {
			public void run() {
				delegate.actionPerformed(event);
			}
		};
		new Thread(runnable).start();
	}

}
