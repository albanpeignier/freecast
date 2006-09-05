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

package org.kolaka.freecast.tracker;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;

import org.apache.commons.lang.Validate;

import com.caucho.hessian.client.HessianProxyFactory;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class HttpMultiTrackerLocator implements TrackerLocator {

	private InetSocketAddress trackerAddress;
  private NetworkIdentifier networkId;

  public HttpMultiTrackerLocator(InetSocketAddress trackerAddress, NetworkIdentifier networkId) {
    Validate.notNull(trackerAddress);
    Validate.notNull(networkId);
    this.trackerAddress = trackerAddress;
    this.networkId = networkId;
  }

	public Tracker resolve() throws TrackerException {
		String url = "http://" + trackerAddress.getHostName() + ":"
				+ trackerAddress.getPort() + "/tracker";

		try {
			HessianProxyFactory factory = new HessianProxyFactory();
			MultiTracker hessianMultiTracker = (MultiTracker) factory.create(MultiTracker.class,
					url);
			return new ProtectedTracker(new MultiTrackerAdapter(networkId, hessianMultiTracker));
		} catch (MalformedURLException e) {
			throw new TrackerException("The tracker url is invalid '" + url
					+ "'", e);
		}
	}

}