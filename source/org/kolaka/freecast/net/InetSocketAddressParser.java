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

package org.kolaka.freecast.net;

import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class InetSocketAddressParser {

	public static final InetSocketAddressParser DEFAULT = new InetSocketAddressParser();

	public InetSocketAddress parse(String string) throws ParseException {
		InetSocketAddress address;

		StringTokenizer st = new StringTokenizer(string, ":", false);
		if (st.countTokens() > 2) {
			throw new ParseException("Invalid SocketAddress " + string, string
					.length());
		} else if (st.countTokens() == 2) {
			address = new InetSocketAddress(st.nextToken(), parsePortNumber(st
					.nextToken()));
		} else {
			address = new InetSocketAddress(parsePortNumber(st.nextToken()));
		}

		return address;
	}

	private int parsePortNumber(String token) throws ParseException {
		try {
			return Integer.parseInt(token);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid port number", 0);
		}
	}

}