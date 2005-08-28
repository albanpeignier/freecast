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

package org.kolaka.freecast.tracker;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;

import com.caucho.hessian.client.HessianProxyFactory;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class HttpTrackerLocator implements TrackerLocator {

    private static final TrackerLocator instance = new HttpTrackerLocator();

    public static TrackerLocator getInstance() {
        return instance;
    }

    public Tracker resolve(InetSocketAddress address) throws TrackerException {
        String url = "http://" + address.getHostName() + ":"
                + address.getPort() + "/tracker";

        try {
            HessianProxyFactory factory = new HessianProxyFactory();
            Tracker hessianTracker = (Tracker) factory.create(Tracker.class, url);
            return new ProtectedTracker(hessianTracker);
        } catch (MalformedURLException e) {
            throw new TrackerException("The tracker url is invalid '" + url
                    + "'", e);
        }
    }

}