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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class OptionalStartable {

	private static final Method startMethod;

	static {
		try {
			startMethod = Startable.class.getMethod("start", new Class[0]);
		} catch (Exception e) {
			throw new UnhandledException(
					"Can't find the method Startable.start", e);
		}
	}

	public static Startable create(final Startable startable,
			Class startableInterface) {
		InvocationHandler handler = new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				try {
					return method.invoke(startable, args);
				} catch (InvocationTargetException e) {
					Throwable targetException = e.getTargetException();
					if (method.equals(startMethod)
							&& targetException instanceof ControlException) {
						LogFactory.getLog(getClass()).debug(
								"OptionalStartable " + startable
										+ " start failed", targetException);
						return null;
					}
					throw targetException;
				}
			}

		};
		return (Startable) Proxy.newProxyInstance(startableInterface
				.getClassLoader(), new Class[] { startableInterface }, handler);
	}

}
