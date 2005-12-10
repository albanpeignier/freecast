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

package org.kolaka.freecast.player;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.kolaka.freecast.cortado.CortadoPanel;
import org.kolaka.freecast.io.SequenceInputStream;
import org.kolaka.freecast.pipe.Consumer;
import org.kolaka.freecast.pipe.ConsumerInputStreamFactory;
import org.kolaka.freecast.service.BaseService;
import org.kolaka.freecast.service.ControlException;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class VideoPlayer extends BaseService implements InteractivePlayer {

	private final CortadoPanel panel;

	private Consumer consumer;

	private ConsumerInputStreamFactory consumerInputStreamFactory;

	private InputStream inputStream;

	public VideoPlayer(CortadoPanel panel) {
		this.panel = panel;
	}

	public PlayerStatus getPlayerStatus() {
		return PlayerStatus.INACTIVE;
	}

	public void setConsumer(Consumer consumer) {
		this.consumer = consumer;
	}

	public void start() throws ControlException {
		super.start();

		consumerInputStreamFactory = new ConsumerInputStreamFactory(consumer);
		Iterator iterator = new Iterator() {
			public Object next() {
				return consumerInputStreamFactory.next();
			}

			public boolean hasNext() {
				return true;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		inputStream = new BufferedInputStream(new SequenceInputStream(iterator));
		panel.setStream(inputStream);
		panel.start();
	}

	public void stop() throws ControlException {
		consumerInputStreamFactory.close();
		panel.stop();
		super.stop();
	}

}
