/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2005 Alban Peignier
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

import org.apache.commons.lang.UnhandledException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class ProxyAuditorProvider implements AuditorProvider {

	private final Auditor auditor;

	public ProxyAuditorProvider(Class auditorInterface) {
		this.auditor = createAuditor(auditorInterface);
	}

	public Auditor getAuditor() {
		return auditor;
	}

	protected abstract Auditor createAuditor(Class auditorInterface);

	protected Auditor createAuditor(Class auditorInterface, InvocationHandler handler) {
		return (Auditor)
			Proxy.newProxyInstance(auditorInterface.getClassLoader(), new Class[]{auditorInterface}, handler);
	}

	protected abstract static class AuditInvocationHandler implements InvocationHandler {

		private static Method equalsMethod;
		private static Method toStringMethod;
		private static Method hashCodeMethod;

		static {
			try {
				equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
				toStringMethod = Object.class.getMethod("toString", new Class[0]);
				hashCodeMethod = Object.class.getMethod("hashCode", new Class[0]);
			} catch (NoSuchMethodException e) {
				throw new UnhandledException("Can't find Object methods", e);
			}
		}

		public Object invoke(Object proxy, Method method, Object[] args)
		        throws Throwable {
			if (equalsMethod.equals(method)) {
				return Boolean.valueOf(equals(Proxy.getInvocationHandler(proxy)));
			} else if (toStringMethod.equals(method)) {
				return toString();
			} else if (hashCodeMethod.equals(method)) {
				return new Integer(hashCode());
			}

            invokeAuditMethod(method, args);
			return null;
		}

		protected abstract void invokeAuditMethod(Method method, Object[] args);

	}


}
