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

package org.kolaka.freecast.peer.test;

import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.NodeStatusProvider;
import org.kolaka.freecast.peer.BasePeerConnection1;
import org.kolaka.freecast.peer.PeerStatus;
import org.kolaka.freecast.peer.event.PeerConnectionStatusListener;
import org.kolaka.freecast.peer.event.VetoablePeerConnectionStatusListener;
import org.kolaka.freecast.transport.MessageReader;
import org.kolaka.freecast.transport.MessageWriter;

import com.mockobjects.ReturnValue;
import com.mockobjects.Verifiable;
import com.mockobjects.util.Verifier;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class MockPeerConnection extends BasePeerConnection1 implements Verifiable {

	public MockPeerConnection(Type type) {
		super(type);
	}
	
	public NodeIdentifier getPeerIdentifier() {
		throw new UnsupportedOperationException();
	}

	private final ReturnValue reader = new ReturnValue("reader");

	private final ReturnValue writer = new ReturnValue("writer");

	protected MessageReader createReader() {
		return (MessageReader) reader.getValue();
	}

	public void setupCreateReader(MessageReader returnedReader) {
		reader.setValue(returnedReader);
	}

	protected MessageWriter createWriter() {
		return (MessageWriter) writer.getValue();
	}

	public void setupCreateWriter(MessageWriter returnedWriter) {
		writer.setValue(returnedWriter);
	}

	public void verify() {
		Verifier.verifyObject(this);
	}

	protected void disposeImpl() {

	}

	public void open() {
		super.open();
	}

	public String toString() {
		return "MockConnection";
	}
	
	public void setNodeStatusProvider(NodeStatusProvider statusProvider) {
		
	}
	
	public void add(VetoablePeerConnectionStatusListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	public void remove(VetoablePeerConnectionStatusListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	public PeerStatus getRemoteStatus() {
		throw new UnsupportedOperationException();
	}
	
}