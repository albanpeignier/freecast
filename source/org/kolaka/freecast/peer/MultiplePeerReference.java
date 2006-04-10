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

package org.kolaka.freecast.peer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

/**
 * <code>PeerReference</code> implementation which uses several PeerReference
 * instances.
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class MultiplePeerReference extends PeerReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 216276991721262538L;

	private Set references;

	private MultiplePeerReference() {

	}

	public MultiplePeerReference(Set references) {
		Validate.notEmpty(references, "No specified PeerReferences");
		Validate.allElementsOfType(references, PeerReference.class);
		this.references = new HashSet(references);
	}

	public Set references() {
		return Collections.unmodifiableSet(references);
	}

	protected String getReferenceString() {
		return references.toString();
	}

	public boolean equals(PeerReference other) {
		return other instanceof MultiplePeerReference
				&& equals((MultiplePeerReference) other);
	}

	public boolean equals(MultiplePeerReference other) {
		return other != null && references.equals(other.references);
	}

	public int hashCode() {
		return references.hashCode();
	}
}
