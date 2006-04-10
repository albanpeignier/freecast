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

package org.kolaka.freecast.transport;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;

public abstract class TimedLoopSender implements Runnable {

	private final long timelength;
	private final String description;
	private long pause = 100;
	
	public TimedLoopSender(final String description) {
		this(description, 5000);
	}
	
	public TimedLoopSender(final String description, final long timelength) {
		Validate.notEmpty(description);
		this.description = description;
		Validate.isTrue(timelength > 0);
		this.timelength = timelength;
	}

	protected void loopStarted() {
		
	}

	protected abstract void send();
	
	protected void loopEnded() {
		
	}
	
	public void run() {
		loopStarted();
		
		try {
		LogFactory.getLog(getClass()).debug("start sending " + description);
		long start = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - start < timelength) {
			send();
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				LogFactory.getLog(getClass()).error("Can't wait thread", e);
				return;
			}
		}
		} finally {
			LogFactory.getLog(getClass()).debug("stop sending " +  description);
			loopEnded();
		}
	}
	
}
