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

package org.kolaka.freecast.node;

import java.io.Serializable;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class Order implements Serializable, Comparable {

	static final long serialVersionUID = -6150355750999858993L;

	public static final Order UNKNOWN = new Order(100);

	public static final Order ZERO = new Order(0);

	/**
	 * <strong>Note: </strong> final fields are supported by the Hessian
	 * serialization
	 */
	private int value;

	private Order(int value) {
		this.value = value;
	}

	public String toString() {
		if (equals(UNKNOWN)) {
			return "unknown";
		}

		return String.valueOf(value);
	}

	public boolean equals(Object o) {
		return o instanceof Order && equals((Order) o);
	}

	public boolean equals(Order other) {
		return value == other.value;
	}

	public int hashCode() {
		return value;
	}

	public int compareTo(Object o) {
		return value - ((Order) o).value;
	}

	public Order lower() {
		if (equals(UNKNOWN)) {
			return this;
		}

		return new Order(value + 1);
	}
	
	/**
	 * Note that order:1 is lower than order:0 ..
	 * 
	 * @param other
	 * @return
	 */
	public boolean isLower(Order other) {
		return value > other.value;
	}

	public static Order getInstance(int value) {
		return new Order(Math.max(0, Math.min(Order.UNKNOWN.value, value)));
	}

}