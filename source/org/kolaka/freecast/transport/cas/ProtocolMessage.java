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

package org.kolaka.freecast.transport.cas;

import java.net.InetSocketAddress;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.enums.ValuedEnum;

public abstract class ProtocolMessage {

	public static class Type extends ValuedEnum {

		private static final long serialVersionUID = 8016928040757804859L;

		public static final Type REGISTRATION = new Type("registration", 0);

		public static final Type CONNECTION_ASSISTANCE = new Type("assistance",
				1);

		public static final Type CONNECTION_REQUEST = new Type("request", 2);

		Type(String name, int value) {
			super(name, value);
		}

		public static Type getType(int value) {
			Type type = (Type) getEnum(Type.class, value);
			Validate.notNull(type, "Unknown value");
			return type;
		}

	}

	public static abstract class ConnectionMessage extends ProtocolMessage {

		private final PendingConnection connection;

		protected ConnectionMessage(PendingConnection connection) {
			this.connection = connection;
		}

		public PendingConnection getPendingConnection() {
			return connection;
		}

	}

	public static class Registration extends ProtocolMessage {

		public static final Type TYPE = Type.REGISTRATION;

		private final InetSocketAddress listenAddress;

		public Registration(InetSocketAddress listenAddress) {
			this.listenAddress = listenAddress;
		}

		public InetSocketAddress getListenAddress() {
			return listenAddress;
		}

		public Type getType() {
			return TYPE;
		}

	}

	public static class ConnectionAssistance extends ConnectionMessage {

		public static final Type TYPE = Type.CONNECTION_ASSISTANCE;

		public ConnectionAssistance(PendingConnection connection) {
			super(connection);
		}

		public Type getType() {
			return TYPE;
		}

	}

	public static class ConnectionRequest extends ConnectionMessage {

		public static final Type TYPE = Type.CONNECTION_REQUEST;

		public ConnectionRequest(PendingConnection connection) {
			super(connection);
		}

		public Type getType() {
			return TYPE;
		}

	}

	public abstract Type getType();

	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
