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
package org.kolaka.freecast.auditor.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.kolaka.freecast.auditor.Auditor;
import org.kolaka.freecast.auditor.AuditorFactory;
import org.kolaka.freecast.auditor.NullAuditorProvider;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class AuditorFactoryTest extends TestCase {

	private AuditorFactory factory;

	protected void setUp() throws Exception {
		super.setUp();
		factory = new AuditorFactory();
	}

	public void testNullAuditor() {
		TestAuditor auditor = (TestAuditor) new NullAuditorProvider(
				TestAuditor.class).getAuditor();
		auditor.auditMethod1(new Object());
		auditor.auditMethod2(new Object(), new Object());
	}

	public void testNoRegisteredAuditor() {
		TestAuditor auditor = (TestAuditor) factory
				.get(TestAuditor.class, this);
		auditor.auditMethod1(new Object());
		auditor.auditMethod2(new Object(), new Object());
	}

	public void testRegisteredAuditor() {
		MockControl mockAuditorControl = MockControl
				.createControl(TestAuditor.class);
		TestAuditor mockAuditor = (TestAuditor) mockAuditorControl.getMock();

		Object auditArgument = new Object();
		mockAuditor.auditMethod1(auditArgument);

		mockAuditorControl.replay();

		factory.register(TestAuditor.class, mockAuditor);

		TestAuditor auditor = (TestAuditor) factory
				.get(TestAuditor.class, this);
		auditor.auditMethod1(auditArgument);

		mockAuditorControl.verify();
	}

	public void testUnregisteredAuditor() {
		MockControl mockAuditorControl = MockControl
				.createControl(TestAuditor.class);
		TestAuditor mockAuditor = (TestAuditor) mockAuditorControl.getMock();

		Object auditArgument = new Object();
		mockAuditor.auditMethod1(auditArgument);

		mockAuditorControl.replay();

		factory.register(TestAuditor.class, mockAuditor);

		TestAuditor auditor = (TestAuditor) factory
				.get(TestAuditor.class, this);
		auditor.auditMethod1(auditArgument);

		factory.unregister(TestAuditor.class, mockAuditor);

		auditor.auditMethod1(auditArgument);

		mockAuditorControl.verify();
	}

	public void testMultipleRegisteredAuditors() {
		Object auditArgument = new Object();

		Collection controls = new LinkedList();
		for (int i = 0; i < 3; i++) {
			MockControl mockAuditorControl = MockControl
					.createControl(TestAuditor.class);
			TestAuditor mockAuditor = (TestAuditor) mockAuditorControl
					.getMock();

			mockAuditor.auditMethod1(auditArgument);

			mockAuditorControl.replay();

			factory.register(TestAuditor.class, mockAuditor);
			controls.add(mockAuditorControl);
		}

		TestAuditor auditor = (TestAuditor) factory
				.get(TestAuditor.class, this);
		auditor.auditMethod1(auditArgument);

		for (Iterator iterator = controls.iterator(); iterator.hasNext();) {
			MockControl mockControl = (MockControl) iterator.next();
			mockControl.verify();
		}
	}

	public interface TestAuditor extends Auditor {

		void auditMethod1(Object value);

		void auditMethod2(Object value1, Object value2);

	}

}
