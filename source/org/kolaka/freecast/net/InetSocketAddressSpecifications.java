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

package org.kolaka.freecast.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.math.IntRange;
import org.kolaka.freecast.lang.math.IntRangeIterator;
import org.kolaka.freecast.lang.math.RandomRangeIterator;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public final class InetSocketAddressSpecifications {

	private InetSocketAddressSpecifications() {

	}

	public static InetSocketAddressSpecification singleton(InetAddress address,
			int port) {
		return singleton(new InetSocketAddress(address, port));
	}

	public static InetSocketAddressSpecification singleton(
			final InetSocketAddress address) {
		return new InetSocketAddressSpecification() {
			public Iterator iterator() {
				return IteratorUtils.singletonIterator(address);
			}
		};
	}

	public static InetSocketAddressSpecification iterator(
			final InetAddress address, final Iterator ports) {
		final Transformer transformer = new Transformer() {
			public Object transform(Object object) {
				Number port = (Number) object;
				return new InetSocketAddress(address, port.intValue());
			}
		};
		return new InetSocketAddressSpecification() {
			public Iterator iterator() {
				return IteratorUtils.transformedIterator(ports, transformer);
			}
		};
	}

	public static InetSocketAddressSpecification portRange(
			final InetAddress address, final IntRange portRange) {
		return iterator(address, new IntRangeIterator(portRange));
	}

	public static InetSocketAddressSpecification randomPortRange(
			final InetAddress address, final IntRange portRange) {
		return iterator(address, new RandomRangeIterator(new IntRangeIterator(portRange)));
	}

	public static InetSocketAddressSpecification portSet(
			final InetAddress address, final Set portSet) {
		return iterator(address, portSet.iterator());
	}

}
