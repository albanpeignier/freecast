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

package org.kolaka.freecast.transport;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class MinaProtocolCodecFactory implements ProtocolCodecFactory {

	private final ProtocolEncoder encoder = new ProtocolEncoderAdapter() {
		public void encode(IoSession session, Object object, ProtocolEncoderOutput output) throws Exception {
			ByteBuffer buffer = ByteBuffer.allocate(1500);
			
			StreamMessageWriter writer = new StreamMessageWriter(buffer.asOutputStream());
			writer.write((Message) object);
			writer.close();
			
			buffer.flip();
			output.write(buffer);
		}
	};
	
	private final ProtocolDecoder decoder = new ProtocolDecoderAdapter() {
		public void decode(IoSession session, ByteBuffer buffer, ProtocolDecoderOutput output) throws Exception {
			Message message = new StreamMessageReader(buffer.asInputStream()).read();
			output.write(message);
		}
	};

	public ProtocolEncoder getEncoder() {
		return encoder;
	}

	public ProtocolDecoder getDecoder() {
		return decoder;
	}
	
	public static ProtocolCodecFilter getFilter() {
		return new ProtocolCodecFilter(new MinaProtocolCodecFactory());
	}

}
