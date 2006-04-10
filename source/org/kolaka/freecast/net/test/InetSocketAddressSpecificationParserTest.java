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

package org.kolaka.freecast.net.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.kolaka.freecast.net.InetSocketAddressSpecification;
import org.kolaka.freecast.net.InetSocketAddressSpecificationParser;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class InetSocketAddressSpecificationParserTest extends TestCase {

	private InetAddress inetAddress;

	protected void setUp() throws Exception {
		super.setUp();
		inetAddress = InetAddress.getLocalHost();
	}

	public void testSingleton() throws ParseException {
		testSpecification("1000", new int[] { 1000 });
	}

	public void testRange() throws ParseException {
		testSpecification("1000-1002", new int[] { 1000, 1001, 1002 });
	}

	public void testSequence() throws ParseException {
		testSpecification("1000,2000", new int[] { 1000, 2000 });
		testSpecification("1000-1002,2000",
				new int[] { 1000, 1001, 1002, 2000 });

		testSpecification(",1000", new int[] { 1000 });
		testSpecification("1000,", new int[] { 1000 });
	}

	public void testParseException() {
		testParseException("abc");
		testParseException("-1");
		testParseException("1000-");
	}

	private void testParseException(String definition) {
		try {
			new InetSocketAddressSpecificationParser().parse(inetAddress,
					definition);
			fail("should throw a ParseException");
		} catch (ParseException e) {
			// expected exception
		}
	}

	private void testSpecification(String definition, int[] expectedPorts)
			throws ParseException {
		testSpecification(new InetSocketAddressSpecificationParser().parse(
				inetAddress, definition), expectedPorts);
	}

	private void testSpecification(
			InetSocketAddressSpecification specification, int[] expectedPorts) {
		Set ports = new HashSet();
		for (Iterator iter = specification.iterator(); iter.hasNext();) {
			InetSocketAddress address = (InetSocketAddress) iter.next();
			assertEquals(inetAddress, address.getAddress());
			ports.add(new Integer(address.getPort()));
		}
		assertEquals(createSet(expectedPorts), ports);
	}

	private static Set createSet(int[] ports) {
		return new HashSet(Arrays.asList(ArrayUtils.toObject(ports)));
	}

}
