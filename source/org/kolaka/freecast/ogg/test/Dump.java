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

package org.kolaka.freecast.ogg.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.ogg.OggStreamSource;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class Dump {

	public static void main(String[] args) throws IOException {
		PrintWriter writer = new PrintWriter(System.out);
		OggSource source = new OggStreamSource(new FileInputStream(args[0]));

		Map absolutePositions = new TreeMap();

		for (int i = 0; i < 20; i++) {
			OggPage page = source.next();

			writer.println("page " + i);
			writer.println("\tfirstpage: " + page.isFirstPage()
					+ "\tlastpage: " + page.isLastPage());
			long absolutePosition = page.getAbsoluteGranulePosition();
			writer.println("\tabsolute granule position: " + absolutePosition);
			String streamSerialNumberString = page
					.getStreamSerialNumberString();
			writer.println("\tstream serial number: "
					+ streamSerialNumberString);

			Long lastAbsolutePosition = (Long) absolutePositions
					.get(streamSerialNumberString);
			if (lastAbsolutePosition != null) {
				writer
						.println("\tabsolute position delta: "
								+ (absolutePosition - lastAbsolutePosition
										.longValue()));
			}
			absolutePositions.put(streamSerialNumberString, new Long(
					absolutePosition));
		}
		writer.close();
	}

}
