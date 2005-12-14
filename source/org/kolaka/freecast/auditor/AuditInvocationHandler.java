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

package org.kolaka.freecast.auditor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class AuditInvocationHandler implements
		InvocationHandler {

	private static final Method EQUALS_METHOD;

	private static final Method TOSTRING_METHOD;

	private static final Method HASHCODE_METHOD;

	static {
		try {
			EQUALS_METHOD = Object.class.getMethod("equals",
					new Class[] { Object.class });
			TOSTRING_METHOD = Object.class.getMethod("toString",
					new Class[0]);
			HASHCODE_METHOD = Object.class.getMethod("hashCode",
					new Class[0]);
		} catch (NoSuchMethodException e) {
			throw new UnhandledException("Can't find Object methods", e);
		}
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (EQUALS_METHOD.equals(method)) {
			return Boolean
					.valueOf(equals(Proxy.getInvocationHandler(proxy)));
		} else if (TOSTRING_METHOD.equals(method)) {
			return toString();
		} else if (HASHCODE_METHOD.equals(method)) {
			return new Integer(hashCode());
		}

		invokeAuditMethod(method, args);
		return null;
	}

	protected abstract void invokeAuditMethod(Method method, Object[] args) throws Throwable;

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}
	
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
}