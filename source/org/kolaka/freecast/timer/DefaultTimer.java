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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultTimer implements Timer {

	private static final long ONE_MINUTE = DateUtils.MILLIS_PER_MINUTE;

	public static long minutes(int count) {
		return ONE_MINUTE * count;
	}

	private static final long ONE_SECOND = DateUtils.MILLIS_PER_SECOND;

	public static long seconds(int count) {
		return ONE_SECOND * count;
	}

	private static final long NO_DELAY = 1;

	public static long nodelay() {
		return NO_DELAY;
	}

	private static final Timer INSTANCE = new DefaultTimer();

	public static final Timer getInstance() {
		return INSTANCE;
	}

	private final PooledExecutor pool;

	private final ClockDaemon clockDaemon;

	private DefaultTimer() {
		pool = new PooledExecutor();

		clockDaemon = new ClockDaemon();
	}

	private void init(Runnable runnable, Object taskID) {
		if (runnable instanceof Task) {
			((Task) runnable).setTaskID(taskID);
		}
	}

	private boolean isCanceled(Runnable runnable) {
		return runnable instanceof Task && ((Task) runnable).isCanceled();
	}

	public void executeLater(Runnable runnable) {
		if (isCanceled(runnable)) {
			LogFactory.getLog(getClass()).debug(
					"ignore canceled task: " + runnable);
		}

		try {
			pool.execute(runnable);
		} catch (InterruptedException e) {
			throw new NotImplementedException(
					"Unsupported InterruptedException", e);
		}
	}

	public void executeAfterDelay(long delay, Runnable runnable) {
		Object taskID = clockDaemon.executeAfterDelay(delay,
				new PooledRunnable(runnable));
		init(runnable, taskID);
	}

	public void executePeriodically(long delay, Runnable runnable,
			boolean startsNow) {
		Object taskID = clockDaemon.executePeriodically(delay, runnable,
				startsNow);
		init(runnable, taskID);
	}

	public void execute(Loop loop) {
		loop.setTimer(this);
		loop.start();
	}

	class PooledRunnable implements Runnable {

		private final Runnable decorated;

		public PooledRunnable(final Runnable decorated) {
			this.decorated = decorated;
		}

		public void run() {
			executeLater(decorated);
		}

	}

}