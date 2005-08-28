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
package org.kolaka.freecast.resource;

import org.apache.commons.lang.Validate;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public interface ResourceLocator {

    public InputStream openResource(URI uri) throws Exception;

    abstract class Exception extends IOException {

        /**
         * Constructs a new exception with the specified detail message.  The
         * cause is not initialized, and may subsequently be initialized by
         * a call to {@link #initCause}.
         *
         * @param   message   the detail message. The detail message is saved for
         *          later retrieval by the {@link #getMessage()} method.
         */
        protected Exception(String message) {
            super(message);
        }

        /**
         * Constructs a new exception with the specified detail message and
         * cause.  <p>Note that the detail message associated with
         * <code>cause</code> is <i>not</i> automatically incorporated in
         * this exception's detail message.
         *
         * @param  message the detail message (which is saved for later retrieval
         *         by the {@link #getMessage()} method).
         * @param  cause the cause (which is saved for later retrieval by the
         *         {@link #getCause()} method).  (A <tt>null</tt> value is
         *         permitted, and indicates that the cause is nonexistent or
         *         unknown.)
         * @since  1.4
         */
        protected Exception(String message, Throwable cause) {
            this(message);
			initCause(cause);
        }

    }

    class MalformedURIException extends Exception {

        public MalformedURIException(URI uri) {
             super("Malformed URI '" + uri + "'");
        }

		public static void checkScheme(URI uri, String scheme) throws MalformedURIException {
			Validate.notNull(uri,"No specified URI");
			if (uri.getScheme() == null || !uri.getScheme().equals(scheme)) {
				throw new MalformedURIException(uri);
			}
		}

    }

    class NoSuchResourceException extends Exception {

        public NoSuchResourceException(URI uri) {
             super("Can't find the resource '" + uri + "'");
        }

    }

    class UnavailableResourceException extends Exception {

        public UnavailableResourceException(URI uri, Throwable cause) {
             super("Can't load the resource '" + uri + "'", cause);
        }

		public UnavailableResourceException(URI uri) {
			super("Can't load the resource '" + uri + "'");
		}

	}

}
