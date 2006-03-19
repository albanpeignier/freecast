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

package org.kolaka.freecast.peer;

import java.util.Iterator;

import org.apache.commons.logging.LogFactory;

public abstract class PeerReferenceProcessor {
	
	public void process(PeerReference reference) {
		try {
			processImpl(reference);
		} catch (Exception e) {
			exceptionCaught(reference, e);
		}
	}
	
	private Exception exception;
	
	public Exception getException() {
		return exception;
	}

	protected void exceptionCaught(PeerReference reference, Exception e) {
		LogFactory.getLog(getClass()).error("Can't process " + reference, e);
	}

	protected void processImpl(PeerReference reference) throws Exception {
		if (reference instanceof InetPeerReference) {
			process((InetPeerReference) reference);
		} else if (reference instanceof MultiplePeerReference) {
			process((MultiplePeerReference) reference);
		} else {  
			throw new IllegalArgumentException("Unsupported reference: " + reference);
		}
	}
	
	protected void process(MultiplePeerReference reference) throws Exception {
		for (Iterator iter=reference.references().iterator(); iter.hasNext(); ) {
			processImpl((PeerReference) iter.next());
		}
	}

	protected abstract void process(InetPeerReference reference) throws Exception;
	
}
