/*
 * This file is part of JSTUN. 
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de>
 *
 * JSTUN is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSTUN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSTUN; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.javawi.jstun.attribute;

import org.apache.commons.lang.enums.ValuedEnum;

public interface MessageAttributeInterface {
	public class MessageAttributeType extends ValuedEnum { 
	    public static final MessageAttributeType Dummy = 
			   new MessageAttributeType("Dummy", DUMMY);
		public static final MessageAttributeType MappedAddress = 
			   new MessageAttributeType("MappedAddress", MAPPEDADDRESS);
		public static final MessageAttributeType ResponseAddress = 
			   new MessageAttributeType("ResponseAddress", RESPONSEADDRESS);
		public static final MessageAttributeType ChangeRequest = 
			   new MessageAttributeType("ChangeRequest", CHANGEREQUEST);
		public static final MessageAttributeType SourceAddress = 
			   new MessageAttributeType("SourceAddress", SOURCEADDRESS);
		public static final MessageAttributeType ChangedAddress = 
			   new MessageAttributeType("ChangedAddress", CHANGEDADDRESS);
		public static final MessageAttributeType Username = 
			   new MessageAttributeType("Username", USERNAME);
		public static final MessageAttributeType Password = 
			   new MessageAttributeType("Password", PASSWORD);
		public static final MessageAttributeType MessageIntegrity = 
			   new MessageAttributeType("MessageIntegrity", MESSAGEINTEGRITY);
		public static final MessageAttributeType ErrorCode = 
			   new MessageAttributeType("ErrorCode", ERRORCODE);
		public static final MessageAttributeType UnknownAttribute = 
			   new MessageAttributeType("UnknownAttribute", UNKNOWNATTRIBUTE);
		public static final MessageAttributeType ReflectedFrom = 
			   new MessageAttributeType("ReflectedFrom", REFLECTEDFROM);

		private MessageAttributeType(String name, int value) {
				super(name, value);
		}
	};
	final static int DUMMY = 0x0000;
	final static int MAPPEDADDRESS = 0x0001;
	final static int RESPONSEADDRESS = 0x0002;
	final static int CHANGEREQUEST = 0x0003;
	final static int SOURCEADDRESS = 0x0004;
	final static int CHANGEDADDRESS = 0x0005;
	final static int USERNAME = 0x0006;
	final static int PASSWORD = 0x0007;
	final static int MESSAGEINTEGRITY = 0x0008;
	final static int ERRORCODE = 0x0009;
	final static int UNKNOWNATTRIBUTE = 0x000a;
	final static int REFLECTEDFROM = 0x000b;
}