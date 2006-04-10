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

package org.kolaka.freecast.auditor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class AuditorFactory {

	private Map auditorProviders = new HashMap();

	private static final AuditorFactory instance = new AuditorFactory();

	public static AuditorFactory getInstance() {
		return instance;
	}

	public Auditor get(Class auditorInterface, Object auditable) {
		Validate.notNull(auditorInterface, "No specified auditor interface");
		Validate.notNull(auditable, "No specified auditable");

		AuditorProvider auditorProvider = getAuditorProvider(auditorInterface);

		Auditor auditor = auditorProvider.getAuditor();
		LogFactory.getLog(getClass()).debug(
				"use auditor implementation " + auditor + " for "
						+ auditorInterface);
		return auditor;
	}

	protected AuditorProvider getAuditorProvider(Class auditorInterface) {
		AuditorProvider auditorProvider = (AuditorProvider) auditorProviders
				.get(auditorInterface);
		if (auditorProvider == null) {
			auditorProvider = createAuditorProvider(auditorInterface);
			auditorProviders.put(auditorInterface, auditorProvider);
		}
		return auditorProvider;
	}

	protected AuditorProvider createAuditorProvider(Class auditorInterface) {
		LogFactory.getLog(getClass()).debug(
				"create empty composite auditor implementation for "
						+ auditorInterface);
		CompositeAuditorProvider auditorProvider = new CompositeAuditorProvider(
				auditorInterface);
		auditorProvider.register(new LogAuditorProvider(auditorInterface)
				.getAuditor());
		return auditorProvider;
	}

	public void register(Class auditorInterface, Auditor auditor) {
		LogFactory.getLog(getClass()).debug(
				"register auditor implementation for " + auditorInterface);
		CompositeAuditorProvider auditorProvider = (CompositeAuditorProvider) getAuditorProvider(auditorInterface);
		auditorProvider.register(auditor);
	}

	public void unregister(Class auditorInterface, Auditor auditor) {
		LogFactory.getLog(getClass()).debug(
				"unregister auditor implementation for " + auditorInterface);
		CompositeAuditorProvider auditorProvider = (CompositeAuditorProvider) getAuditorProvider(auditorInterface);
		auditorProvider.unregister(auditor);
	}

}
