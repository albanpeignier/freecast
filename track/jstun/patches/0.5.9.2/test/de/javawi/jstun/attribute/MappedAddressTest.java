/*
 * This file is part of JSTUN.
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de>
 *
 * JSTUN is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSTUN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSTUN; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.javawi.jstun.attribute;

import junit.framework.TestCase;

public class MappedAddressTest extends TestCase {
	MappedAddress ma;
	byte[] data;
	public MappedAddressTest(String mesg) {
		super(mesg);
	}
	
	public void setUp() throws Exception {
		data = new byte[8];
		data[0] = 0;
		data[1] = 1;
		data[2] = -8;
		data[3] = 96;
		data[4] = 84;
		data[5] = 56;
		data[6] = -23;
		data[7] = 76;
		ma = (MappedAddress) MappedAddress.parse(data);
	}

	/*
	 * Test method for 'de.javawi.jstun.attribute.MappedAddress.MappedAddress()'
	 */
	public void testMappedAddress() {
		new MappedAddress();
	}

	/*
	 * Test method for 'de.javawi.jstun.attribute.MappedResponseChangedSourceAddressReflectedFrom.getBytes()'
	 */
	public void testGetBytes() {
		try {
			byte[] result = ma.getBytes();

			assertTrue(result[0] == 0);
			assertTrue(result[1] == 1);
			assertTrue(result[2] == 0);
			assertTrue(result[3] == 8);
			assertTrue(result[4] == data[0]);
			assertTrue(result[5] == data[1]);
			assertTrue(result[6] == data[2]);
			assertTrue(result[7] == data[3]);
			assertTrue(result[8] == data[4]);
			assertTrue(result[9] == data[5]);
			assertTrue(result[10] == data[6]);
			assertTrue(result[11] == data[7]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Test method for 'de.javawi.jstun.attribute.MappedResponseChangedSourceAddressReflectedFrom.getPort()'
	 */
	public void testGetPort() {
		assertTrue(ma.getPort() == 63584);
	}

	/*
	 * Test method for 'de.javawi.jstun.attribute.MappedResponseChangedSourceAddressReflectedFrom.getAddress()'
	 */
	public void testGetAddress() {
		try {
			System.out.println(ma.getAddress().toString());
			assertTrue(ma.getAddress().equals(new de.javawi.jstun.util.Address("84.56.233.76")));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Test method for 'de.javawi.jstun.attribute.MappedResponseChangedSourceAddressReflectedFrom.setPort(int)'
	 */
	public void testSetPort() {

	}

	/*
	 * Test method for 'de.javawi.jstun.attribute.MappedResponseChangedSourceAddressReflectedFrom.setAddress(Address)'
	 */
	public void testSetAddress() {

	}

	/*
	 * Test method for 'de.javawi.jstun.attribute.MappedResponseChangedSourceAddressReflectedFrom.toString()'
	 */
	public void testToString() {

	}

}
