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

package org.kolaka.freecast.pipe;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.packet.LogicalPage;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ConsumerInputStreamFactory {

	private Consumer consumer;

	public ConsumerInputStreamFactory(Consumer consumer) {
		this.consumer = consumer;
	}

	private long delayOnEmptyPipe = 5000;

	private Stream next = null;

	public InputStream next() {
		if (next == null) {
			next = new Stream(nextPage());
		}

		return next;
	}

	private LogicalPage nextPage() {
		LogicalPage readPage = null;

		do {
			try {
				readPage = consumer.consume();
			} catch (EmptyPipeException e) {
				sleep();
			}
		} while (readPage == null);

		return readPage;
	}

	class Stream extends InputStream {

		private LogicalPage readPage;

		private int readIndex;

		Stream(LogicalPage readPage) {
			Validate.isTrue(readPage.isFirstPage(),
					"read page isn't first page: " + readPage);
			this.readPage = readPage;
		}

		public int read() throws IOException {
			if (readPage == null) {
				readPage = nextPage();
				readIndex = 0;
				LogFactory.getLog(getClass()).trace("read new page: " + readPage);

				if (readPage.isFirstPage()) {
					LogFactory.getLog(getClass()).trace("new input stream");
					next = new Stream(readPage);
					return -1;
				}
			}

			if (closed) {
				return -1;
			}

			byte bytes[] = readPage.getBytes();
			int read = bytes[readIndex] & 0xff;
			readIndex++;

			if (readIndex >= bytes.length) {
				readPage = null;
			}

			return read;
		}

		private boolean closed;

		public void close() {
			closed = true;
		}

	}

	private void sleep() {
		String msg = "pipe is empty. Sleep " + delayOnEmptyPipe / 1000
				+ " seconds";
		LogFactory.getLog(getClass()).trace(msg);

		try {
			Thread.sleep(delayOnEmptyPipe);
		} catch (InterruptedException e) {
			LogFactory.getLog(getClass()).error("Can make the thead sleep", e);
		}
	}

	public void close() {
		consumer.close();
	}

}