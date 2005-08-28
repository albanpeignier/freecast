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
package org.kolaka.freecast.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.LinkedList;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class TextFile {

	private List lines;

	public void load(InputStream input) throws IOException {
		load(new BufferedReader(new InputStreamReader(input)));
	}

	public void load(BufferedReader reader) throws IOException {
	    List lines = new LinkedList();

	    int lineNumber = 0;
	    String lineContent;
	    while ((lineContent = reader.readLine()) != null) {
	        lines.add(new Line(++lineNumber, lineContent));
	    }

	    this.lines = lines;
	}

	protected void checkLoading() throws IOException {
	    if (!isLoaded()) {
	        throw new IOException("not loaded text file");
	    }
	}

	protected boolean isLoaded() {
		return lines != null;
	}

	public int getLineCount() throws IOException {
	    checkLoading();
	    return lines.size();
	}

	public List getLines() throws IOException {
	    checkLoading();
	    return lines;
	}

	public static class Line {

	    private final int number;

	    private final String content;

	    public Line(int number, String content) {
	        this.number = number;
	        this.content = content;
	    }

	    public int getNumber() {
	        return number;
	    }

	    public String getContent() {
	        return content;
	    }

	}
}
