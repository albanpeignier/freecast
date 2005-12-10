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

package org.kolaka.freecast.ogg.test;

import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.ogg.OggSource;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class MemoryOggSource implements OggSource {

	private List pages = new LinkedList();

	public OggPage next() throws IOException {
		if (isEmpty()) {
			throw new EOFException();
		}

		return (OggPage) pages.remove(0);
	}

	public void add(OggPage page) {
		Validate.notNull(page);
		pages.add(page);
	}

	public boolean isEmpty() {
		return pages.isEmpty();
	}

	public void close() throws IOException {
	}

}
