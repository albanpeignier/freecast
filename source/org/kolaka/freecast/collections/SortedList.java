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

package org.kolaka.freecast.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.list.AbstractListDecorator;
import org.apache.commons.lang.NotImplementedException;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class SortedList extends AbstractListDecorator {

	private final Comparator comparator;

	public SortedList() {
		this(new ArrayList(), ComparableComparator.getInstance());
	}

	public SortedList(Comparator comparator) {
		this(new ArrayList(), comparator);
	}

	public SortedList(List list, Comparator comparator) {
		super(sortInitialList(list, comparator));
		this.comparator = comparator;
	}

	private static List sortInitialList(List list, Comparator comparator) {
		Collections.sort(list, comparator);
		return list;
	}

	public void add(int arg0, Object arg1) {
		throw new NotImplementedException(getClass());
	}

	public boolean addAll(int arg0, Collection arg1) {
		throw new NotImplementedException(getClass());
	}

	public Object set(int arg0, Object arg1) {
		throw new NotImplementedException(getClass());
	}

	public int findIndexOf(Object element) {
		int binarySearchIndex = Collections.binarySearch(getList(), element,
				comparator);

		int index;
		if (binarySearchIndex >= 0) {
			index = binarySearchIndex;
		} else {
			index = -(binarySearchIndex + 1);
		}

		return index;
	}

	public boolean add(Object element) {
		super.add(findIndexOf(element), element);
		return true;
	}

	public boolean addAll(Collection elements) {
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			add(iter.next());
		}
		return !elements.isEmpty();
	}

}