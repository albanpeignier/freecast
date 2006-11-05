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

package org.kolaka.freecast.timer.test;

import junit.framework.TestCase;

import org.apache.commons.lang.time.DateUtils;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Task;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class TimerTest extends TestCase {

	private static final long LATENCY = 100;

	public void testExecuteLater() throws InterruptedException {
		TestRunnable testRunnable = new TestRunnable();
		DefaultTimer.getInstance().executeLater(testRunnable);

		Thread.sleep(LATENCY);

		assertNotNull("runnable not executed", testRunnable.getThread());
		assertNotSame("same thread used", Thread.currentThread(), testRunnable
				.getThread());
	}

	static class TestRunnable implements Runnable {

		private Thread thread;

		public void run() {
			thread = Thread.currentThread();
		}

		public Thread getThread() {
			return thread;
		}
	}

	public void testExecuteAt() throws InterruptedException {
		final long delay = DateUtils.MILLIS_PER_SECOND / 4;
		final long timeLength = DateUtils.MILLIS_PER_SECOND / 4;

		PausedRunnable runnable = new PausedRunnable(timeLength);
		DefaultTimer.getInstance().executeAfterDelay(delay, runnable);

		assertEquals("runnable shouldn't be started", PausedRunnable.CREATED,
				runnable.getStatus());

		Thread.sleep(delay + LATENCY);

		assertEquals("runnable should be started", PausedRunnable.STARTED,
				runnable.getStatus());

		Thread.sleep(timeLength + LATENCY);

		assertEquals("runnable should be done", PausedRunnable.DONE, runnable
				.getStatus());
	}

	public void testExecuteAtConcurrency() throws InterruptedException {
		final long delay = DateUtils.MILLIS_PER_SECOND / 4;
		final long timeLength = DateUtils.MILLIS_PER_SECOND / 4;

		PausedRunnable firstRunnable = new PausedRunnable(timeLength);
		PausedRunnable secondRunnable = new PausedRunnable(timeLength * 2);

		DefaultTimer.getInstance().executeAfterDelay(delay, firstRunnable);
		DefaultTimer.getInstance().executeAfterDelay(delay, secondRunnable);

		assertEquals("runnable shouldn't be started", PausedRunnable.CREATED,
				firstRunnable.getStatus());
		assertEquals("runnable shouldn't be started", PausedRunnable.CREATED,
				secondRunnable.getStatus());

		Thread.sleep(delay + LATENCY);

		assertEquals("runnable should be started", PausedRunnable.STARTED,
				firstRunnable.getStatus());
		assertEquals("second runnable should be started",
				PausedRunnable.STARTED, secondRunnable.getStatus());

		Thread.sleep(timeLength + LATENCY);

		assertEquals("first runnable should be done", PausedRunnable.DONE,
				firstRunnable.getStatus());
		assertEquals("second runnable shouldn't be done",
				PausedRunnable.STARTED, secondRunnable.getStatus());

		Thread.sleep(timeLength + LATENCY);
		assertEquals("second runnable should be done", PausedRunnable.DONE,
				secondRunnable.getStatus());
	}

	public void testExecuteAtCancel() throws InterruptedException {
		final long delay = DateUtils.MILLIS_PER_SECOND / 4;
		final long timeLength = DateUtils.MILLIS_PER_SECOND / 4;

		PausedRunnable runnable = new PausedRunnable(timeLength);
		DefaultTimer.getInstance().executeAfterDelay(delay, runnable);

		assertEquals("runnable shouldn't be started", PausedRunnable.CREATED,
				runnable.getStatus());

		Thread.sleep(LATENCY);

		runnable.cancel();

		assertEquals("runnable should be canceled", PausedRunnable.CANCELED,
				runnable.getStatus());
	}

	static class PausedRunnable extends Task {

		public static final int CREATED = 0;

		public static final int STARTED = 1;

		public static final int DONE = 2;

		public static final int ABORTED = 3;

		public static final int CANCELED = 4;

		private final long timeLength;

		private int status = CREATED;

		PausedRunnable(long timeLength) {
			this.timeLength = timeLength;
		}

		public void cancel() {
			super.cancel();
			status = CANCELED;
		}

		public void run() {
			status = STARTED;
			try {
				Thread.sleep(timeLength);
			} catch (InterruptedException e) {
				status = ABORTED;
			}
			status = DONE;
		}

		public int getStatus() {
			return status;
		}
	}

}