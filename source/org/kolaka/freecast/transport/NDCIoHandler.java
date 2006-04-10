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

package org.kolaka.freecast.transport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.NDC;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;

public class NDCIoHandler implements IoHandler {
	
	private final String defaultContext;
	private final IoHandler delegate;

	public NDCIoHandler(final IoHandler delegate) {
		this(delegate, null);
	}
	
	public NDCIoHandler(final IoHandler delegate, final String defaultContext) {
		Validate.notNull(delegate);
		this.delegate = delegate;
		
		this.defaultContext = defaultContext;
	}

	private void execute(IoSession session, Command command) throws Exception {
		String context = StringUtils.isEmpty(defaultContext) ? getContext(session) : defaultContext; 
		NDC.push(context);
		
		try {
			command.run();
		} finally {
			NDC.pop();
		}
	}
	
	protected String getContext(IoSession session) {
		return String.valueOf(session.getRemoteAddress()); 
	}

	static interface Command {
		
		void run() throws Exception;
		
	}

	public void sessionCreated(final IoSession session) throws Exception {
		execute(session, new Command() {
			public void run() throws Exception {
				delegate.sessionCreated(session);
			};
		});
	}

	public void sessionOpened(final IoSession session) throws Exception {
		execute(session, new Command() {
			public void run() throws Exception {
				delegate.sessionOpened(session);
			};
		});
	}

	public void sessionClosed(final IoSession session) throws Exception {
		execute(session, new Command() {
			public void run() throws Exception {
				delegate.sessionClosed(session);
			};
		});
	}

	public void sessionIdle(final IoSession session, final IdleStatus status)
			throws Exception {
		execute(session, new Command() {
			public void run() throws Exception {
				delegate.sessionIdle(session, status);
			};
		});
	}

	public void exceptionCaught(final IoSession session, final Throwable cause)
			throws Exception {
		execute(session, new Command() {
			public void run() throws Exception {
				delegate.exceptionCaught(session, cause);
			};
		});
	}

	public void messageReceived(final IoSession session, final Object message)
			throws Exception {
		execute(session, new Command() {
			public void run() throws Exception {
				delegate.messageReceived(session, message);
			};
		});
	}

	public void messageSent(final IoSession session, final Object message) throws Exception {
		execute(session, new Command() {
			public void run() throws Exception {
				delegate.messageSent(session, message);
			};
		});
	}

}
