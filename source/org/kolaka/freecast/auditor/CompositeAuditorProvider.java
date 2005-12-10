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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class CompositeAuditorProvider implements AuditorProvider {

	private final Class auditorInterface;

	private final Set auditors = new HashSet();

	private final Auditor proxyAuditor;

	public CompositeAuditorProvider(Class auditorInterface) {
		this.auditorInterface = auditorInterface;
		this.proxyAuditor = createAuditor();
	}

	public void register(Auditor auditor) {
		Validate.isTrue(auditorInterface.isInstance(auditor),
				"The specified Auditor doesn't implement " + auditorInterface);
		auditors.add(auditor);
	}

	public void unregister(Auditor auditor) {
		auditors.remove(auditor);
	}

	public Auditor getAuditor() {
		return proxyAuditor;
	}

	protected Auditor createAuditor() {
		return (Auditor) Proxy.newProxyInstance(auditorInterface
				.getClassLoader(), new Class[] { auditorInterface }, handler);
	}

	private InvocationHandler handler = new InvocationHandler() {

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (method.getName().equals("toString")) {
				return "CompositeAuditor" + auditors;
			}

			for (Iterator iterator = auditors.iterator(); iterator.hasNext();) {
				Auditor auditor = (Auditor) iterator.next();
				method.invoke(auditor, args);
			}
			return null;
		}

	};

}
