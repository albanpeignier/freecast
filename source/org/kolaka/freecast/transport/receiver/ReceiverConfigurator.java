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

package org.kolaka.freecast.transport.receiver;

import java.io.IOException;

public abstract class ReceiverConfigurator {

	public static ReceiverConfigurator getInstance(SourceReceiverConfiguration configuration) {
		if (configuration instanceof PlaylistEncoderReceiverConfiguration) {
			return new  PlaylistEncoderReceiverConfigurator();
		} else if (configuration instanceof PlaylistReceiverConfiguration) {
			return new  PlaylistReceiverConfigurator();
		} else if (configuration instanceof ShoutClientReceiverConfiguration) {
			return new  ShoutClientReceiverConfigurator();
		} else if (configuration instanceof ShoutServerReceiverConfiguration) {
			return new  ShoutServerReceiverConfigurator();
		}
		
		throw new IllegalArgumentException("Unsupported configuration: " + configuration);
	}
	
	public abstract SourceReceiver configure(ReceiverConfiguration configuration) throws IOException;

}
