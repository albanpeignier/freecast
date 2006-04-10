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

package org.kolaka.freecast.timer;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;

public abstract class Loop implements TimerUser {

	private Timer timer;

	public void setTimer(Timer timer) {
		Validate.notNull(timer, "No specified Timer");
		this.timer = timer;
	}

	protected abstract long loop() throws LoopInterruptedException;

	private Task task = new Task() {

		public void run() {
			if (isCanceled()) {
				return;
			}

			long delay = 0;

			try {
				delay = loop();
				if (delay <= 0) {
					throw new LoopInterruptedException(
							"negative delay returned (" + delay + ")");
				}
			} catch (LoopInterruptedException e) {
				LogFactory.getLog(getClass()).error(
						"loop interrupted: " + Loop.this, e);
				return;
			}

			if (!isCanceled()) {
				timer.executeAfterDelay(delay, this);
			}
		}

	};

	public void start() {
		if (timer == null) {
			throw new IllegalStateException("No defined Timer for " + this);
		}

		timer.executeLater(task);
	}

	public void cancel() {
		task.cancel();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}