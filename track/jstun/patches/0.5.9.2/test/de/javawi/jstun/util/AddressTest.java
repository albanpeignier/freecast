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

package de.javawi.jstun.util;

import junit.framework.TestCase;

public class AddressTest extends TestCase {
	Address address;
	
	public AddressTest(String mesg) {
		super(mesg);
	}

	protected void setUp() throws Exception {
		address = new Address("192.168.100.1");
	}

	/*
	 * Test method for 'de.javawi.jstun.util.Address.Address(int, int, int, int)'
	 */
	public void testAddressIntIntIntInt() {
		try {
			Address comp = new Address(192,168,100,1);
			assertTrue(address.equals(comp));
		} catch (UtilityException ue) {
			ue.printStackTrace();
		}
	}

	/*
	 * Test method for 'de.javawi.jstun.util.Address.Address(String)'
	 */
	public void testAddressString() {
		try {
			Address comp = new Address("192.168.100.1");
			assertTrue(address.equals(comp));
		} catch (UtilityException ue) {
			ue.printStackTrace();
		}
	}

	/*
	 * Test method for 'de.javawi.jstun.util.Address.Address(byte[])'
	 */
	public void testAddressByteArray() {
		try {
			byte[] data = {(byte)192, (byte)168, (byte)100, (byte)1};
			Address comp = new Address(data);
			assertTrue(address.equals(comp));
		} catch (UtilityException ue) {
			ue.printStackTrace();
		}
	}

	/*
	 * Test method for 'de.javawi.jstun.util.Address.toString()'
	 */
	public void testToString() {
		try {
			Address comp = new Address("192.168.100.1");
			assertTrue(address.equals(comp));
		} catch (UtilityException ue) {
			ue.printStackTrace();
		}
	}

	/*
	 * Test method for 'de.javawi.jstun.util.Address.getBytes()'
	 */
	public void testGetBytes() {
		try {
			byte[] data = address.getBytes();
			assertTrue(data[0] == (byte)192);
			assertTrue(data[1] == (byte)168);
			assertTrue(data[2] == (byte)100);
			assertTrue(data[3] == (byte)1);
		} catch (UtilityException ue) {
			ue.printStackTrace();
		}
		
	}

	/*
	 * Test method for 'de.javawi.jstun.util.Address.getInetAddress()'
	 */
	public void testGetInetAddress() {
		try {
			Address comp = new Address("192.168.100.1");
			assertTrue(address.getInetAddress().equals(comp.getInetAddress()));
			comp = new Address("192.168.100.2");
			assertFalse(address.getInetAddress().equals(comp.getInetAddress()));
		} catch (UtilityException ue) {
			ue.printStackTrace();
		} catch (java.net.UnknownHostException uhe) {
			uhe.printStackTrace();
		}
	}

	/*
	 * Test method for 'de.javawi.jstun.util.Address.equals(Object)'
	 */
	public void testEqualsObject() {
		try {
			Address comp = new Address("192.168.100.1");
			assertTrue(address.equals(comp));
			comp = new Address("192.168.100.2");
			assertFalse(address.equals(comp));
		} catch (UtilityException ue) {
			ue.printStackTrace();
		}
	}

}
