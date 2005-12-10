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
package org.kolaka.freecast.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class InetSocketAddressSpecificationParser {

	public InetSocketAddressSpecification parse(String address,
			String definition) throws ParseException, UnknownHostException {
		return parse(InetAddress.getByName(address), definition);
	}

	public InetSocketAddressSpecification parse(InetAddress address,
			String definition) throws ParseException {
		if (StringUtils.contains(definition, ",")) {
			StringTokenizer chainParts = new StringTokenizer(definition, ",",
					false);
			InetSocketAddressSpecificationChain chain = new InetSocketAddressSpecificationChain();
			while (chainParts.hasMoreTokens()) {
				chain.add(parse(address, chainParts.nextToken()));
			}
			return chain;
		}
		StringTokenizer rangeParts = new StringTokenizer(definition, "-", true);
		switch (rangeParts.countTokens()) {
		case 1:
			return InetSocketAddressSpecifications.singleton(address,
					parsePort(rangeParts.nextToken()));
		case 3:
			int firstPort = parsePort(rangeParts.nextToken());
			if (!rangeParts.nextToken().equals("-")) {
				throw new ParseException("Invalid port range: " + definition, 0);
			}
			int secondPort = parsePort(rangeParts.nextToken());
			IntRange range = new IntRange(firstPort, secondPort);
			return InetSocketAddressSpecifications.portRange(address, range);
		default:
			throw new ParseException("Invalid specification: " + definition,
					definition.length());
		}
	}

	private int parsePort(String definition) throws ParseException {
		try {
			return Integer.parseInt(definition);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid port number:" + definition, 0);
		}
	}

}
