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

package org.kolaka.freecast.resource;

import java.net.URI;
import java.net.URISyntaxException;

public class URIParser {

	private static final char WIN32_FILESEPARATOR = '\\';

	public URI parse(String uriDefinition) throws URISyntaxException {
		uriDefinition = filterWin32Path(uriDefinition);
		return new URI(encode(uriDefinition));
	}
	
	private String filterWin32Path(String uriDefinition) {
		boolean isWin32Path = uriDefinition.length() > 2 &&
			Character.isLetter(uriDefinition.charAt(0)) &&
			uriDefinition.charAt(1) == WIN32_FILESEPARATOR;
		if (!isWin32Path) {
			return uriDefinition;
		}
		return "file:/" + uriDefinition.charAt(0) + "/";
	}
	

	private String encode(String uriDefinition) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < uriDefinition.length(); i++) {
			char c = uriDefinition.charAt(i);
			if (c == WIN32_FILESEPARATOR) {
				sb.append('/');
			} else if (isSpecialCharacter(c)) {
				sb.append('%');
				sb.append(Integer.toHexString((c >> 4) & 0x0f));
				sb.append(Integer.toHexString((c >> 0) & 0x0f));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private static String reservedCharacters = "./?:@&=+%_-";
	
	/**
	 * @param c
	 * @return
	 */
	private boolean isSpecialCharacter(char c) {
		return reservedCharacters.indexOf(c) == -1 && !Character.isLetterOrDigit(c); 
	}
	

}
