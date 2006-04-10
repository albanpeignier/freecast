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

package org.kolaka.freecast.service;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class BaseService implements Service {

	private Startable.Status status = Startable.Status.CREATED;

	public BaseService() {

	}

	private ServiceListenerSupport support = new ServiceListenerSupport(this);

	public void add(Service.Listener listener) {
		support.add(listener);
	}

	public void remove(Service.Listener listener) {
		support.remove(listener);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public void init() throws ControlException {
		LogFactory.getLog(getClass()).debug("init");
		support.fireInitialized();
	}

	public void start() throws ControlException {
		LogFactory.getLog(getClass()).debug("started");
		status = Startable.Status.STARTED;
		support.fireStarted();
	}

	public Startable.Status getStatus() {
		return status;
	}

	protected boolean isStopped() {
		return status.equals(Startable.Status.STOPPED);
	}

	public void stop() throws ControlException {
		LogFactory.getLog(getClass()).debug("stopped");
		status = Startable.Status.STOPPED;
		support.fireStopped();
	}

	protected void stopQuietly() {
		Controlables.stopQuietly(this);
	}

	public void dispose() throws ControlException {
		LogFactory.getLog(getClass()).debug("dispose");
		support.fireDisposed();
	}
}