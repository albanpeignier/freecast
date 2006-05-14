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

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class LoopService extends BaseService {

	private Loop loop;

	private static int threadIdentifier = 1;

	public void start() throws ControlException {
		super.start();

		loop = createLoop();
		if (loop == null) {
			throw new ControlException("the created loop is null");
		}

		String threadName = ClassUtils.getShortClassName(getClass()) + "-"
				+ threadIdentifier;
		Thread thread = new Thread(new LoopRunnable(), threadName);
		thread.start();
	}

	protected abstract Loop createLoop();

	class LoopRunnable implements Runnable {

		public void run() {
			try {
				while (!isStopped()) {
					long delay = loop.loop();
					if (delay < 0) {
						throw new IllegalStateException("Negative delay "
								+ delay);
					}
					Thread.sleep(delay);
				}
			} catch (LoopInterruptedException e) {
				LogFactory.getLog(getClass()).warn(
						"Loop interrupted " + LoopService.this, e);
				stopQuietly();
			} catch (Throwable e) {
				LogFactory.getLog(getClass()).error(
						"Loop execution failed in " + LoopService.this, e);
			}
		}

	}

	public static interface Loop {

		public long loop() throws LoopInterruptedException;

	}

	public static class LoopInterruptedException extends Exception {

		private static final long serialVersionUID = 3258415044936217397L;

		public LoopInterruptedException(String message) {
			super(message);
		}

		public LoopInterruptedException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}