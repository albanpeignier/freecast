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

package org.kolaka.freecast.lang.mutable.test;

import java.util.Observer;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.kolaka.freecast.lang.mutable.ObservableValue;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ObservableValueTest extends TestCase {

	public void testSetValue() {
		Object initialValue = new Object();
		ObservableValue value = new ObservableValue(initialValue);
		assertEquals(initialValue, value.getValue());

		Object newValue = new Object();

		MockControl observerControl = MockControl.createControl(Observer.class);
		Observer observer = (Observer) observerControl.getMock();

		observer.update(value, newValue);

		observerControl.replay();

		value.addObserver(observer);
		assertEquals(1, value.countObservers());

		value.setValue(newValue);
		assertEquals(newValue, value.getValue());

		observerControl.verify();
	}

}
