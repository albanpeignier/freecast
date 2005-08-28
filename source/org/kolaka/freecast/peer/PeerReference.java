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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class PeerReference implements Serializable {

    static final long serialVersionUID = 3189920898117152786L;

    public String toString() {
        return getReferenceString() + attributes;
    }

    protected abstract String getReferenceString();

    public boolean equals(Object o) {
        return o instanceof PeerReference && equals((PeerReference) o);
    }

    public abstract boolean equals(PeerReference other);

    public abstract int hashCode();

    private Map attributes = new TreeMap();

    public static final String IDENTIFIER_ATTRIBUTE = "identifier";

    public static final String ORDER_ATTRIBUTE = "order";

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

}