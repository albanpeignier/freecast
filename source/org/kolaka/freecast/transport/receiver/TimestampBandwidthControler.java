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

package org.kolaka.freecast.transport.receiver;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.kolaka.freecast.packet.LogicalPage;
import org.kolaka.freecast.timer.TimeBase;

public class TimestampBandwidthControler implements BandwidthControler {

	private TimeBase timeBase = TimeBase.DEFAULT;

	public void setTimerBase(TimeBase timeBase) {
		Validate.notNull(timeBase);
		this.timeBase = timeBase;
	}

	private static final long UNDEFINED = -1;

	private long initialTimeMillis = UNDEFINED,
			initialPageTimestamp = UNDEFINED;

	public long getTimeDelay(LogicalPage page) {
		long now = timeBase.currentTimeMillis();

		if (initialPageTimestamp == UNDEFINED) {
			initialPageTimestamp = page.getTimestamp();
			initialTimeMillis = now;

			return 0;
		}

		long expectedPageTimeMillis = (page.getTimestamp() - initialPageTimestamp)
				+ initialTimeMillis;
		long delay = expectedPageTimeMillis - now;
		if (delay > (DateUtils.MILLIS_PER_SECOND * 10)) {
			throw new IllegalStateException("Delay shouldn't be so high (" + delay + ")");
		}
		return delay;
	}

}
