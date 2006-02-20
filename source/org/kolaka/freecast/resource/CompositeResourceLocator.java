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
package org.kolaka.freecast.resource;

import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class CompositeResourceLocator implements ResourceLocator {

	private List resourceLocators = new LinkedList();

	public void add(ResourceLocator resourceLocator) {
		resourceLocators.add(resourceLocator);
	}

	public InputStream openResource(URI uri) throws ResourceLocator.Exception {
		Validate.notNull(uri, "No specified URI");

		for (Iterator iterator = resourceLocators.iterator(); iterator
				.hasNext();) {
			ResourceLocator resourceLocator = (ResourceLocator) iterator.next();
			try {
				return resourceLocator.openResource(uri);
			} catch (Exception e) {
				LogFactory.getLog(getClass()).trace(
						resourceLocator + " can't open " + uri);
				continue;
			}
		}
		throw new ResourceLocator.UnavailableResourceException(uri);
	}

}
