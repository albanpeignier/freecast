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

package org.kolaka.freecast.node;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public final class NodeIdentifier implements Comparable, Serializable {

	static final long serialVersionUID = -4121852123565706283L;

	/**
	 * <strong>Note: </strong> final fields are supported by the Hessian
	 * serialization
	 */
	private long value;

	public NodeIdentifier(long value) {
		this.value = value;
	}

	public boolean equals(Object o) {
		return o instanceof NodeIdentifier && equals((NodeIdentifier) o);
	}

	public boolean equals(NodeIdentifier identifier) {
		return identifier != null && value == identifier.value;
	}

	public int hashCode() {
		return new HashCodeBuilder().append(value).toHashCode();
	}
	
	public long longValue() {
		return value;
	}

	public String toString() {
		return "#"
				+ StringUtils.leftPad(Long.toHexString(value).toUpperCase(),
						16, 'F');
	}

	public int compareTo(Object o) {
		return NumberUtils.compare(value, ((NodeIdentifier) o).value);
	}

}