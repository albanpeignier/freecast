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

package org.kolaka.freecast.transport.receiver;

import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.LoopService;
import org.kolaka.freecast.service.Startable;
import org.kolaka.freecast.timer.DefaultTimer;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class OggSourceFactoryReceiver extends OggSourceReceiver {

	private final OggSourceFactory factory;

	private final ReceiverConfiguration configuration;
	
	public ReceiverConfiguration getReceiverConfiguration() {
		return configuration;
	}

	public OggSourceFactoryReceiver(OggSourceFactory factory, ReceiverConfiguration configuration) {
		Validate.notNull(factory, "No specified OggSourceFactory");
		this.factory = factory;
		Validate.notNull(configuration, "No specified ReceiverConfiguration");
		this.configuration = configuration;
	}

	public void start() throws ControlException {
		if (factory instanceof Startable) {
			((Startable) factory).start();
		}
		super.start();
	}

	public void stop() throws ControlException {
		if (factory instanceof Startable) {
			((Startable) factory).stop();
		}
		super.stop();
	}

	protected LoopService.Loop createLoop() {
		return new LoopService.Loop() {

			public long loop() throws LoopInterruptedException {
				OggSource oggSource;

				try {
					oggSource = factory.next();
				} catch (IOException e) {
					String message = "can't create next OggSource with "
							+ factory + ", wait 10 secondes before retrying";
					LogFactory.getLog(getClass()).error(message, e);
					return DefaultTimer.seconds(10);
				}

				LogFactory.getLog(getClass()).debug(
						"change source for " + oggSource);

				StopWatch receivingWatch = new StopWatch();
				receivingWatch.start();

				long delay = DefaultTimer.nodelay();

				try {
					receive(oggSource);
				} catch (EOFException e) {
					LogFactory.getLog(getClass()).debug(
							"end of source " + oggSource, e);
				} catch (Exception e) {
					LogFactory.getLog(getClass()).error(
							"stream reception failed with  " + oggSource, e);
				} finally {
					try {
						oggSource.close();
					} catch (IOException e) {
						LogFactory.getLog(getClass()).debug(
								"can't close properly the OggSource "
										+ oggSource, e);
					}

					/*
					 * to avoid crazy loops, mark a pause if needed
					 */
					receivingWatch.stop();
					if (receivingWatch.getTime() < 3 * DateUtils.MILLIS_PER_SECOND) {
						delay = DefaultTimer.seconds(3);
					}
				}

				return delay;
			}
		};
	}
	
}
