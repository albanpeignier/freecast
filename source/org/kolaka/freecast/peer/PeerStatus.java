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
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kolaka.freecast.node.NodeIdentifier;
import org.kolaka.freecast.node.Order;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PeerStatus implements Serializable {

    static final long serialVersionUID = 2356337954683447228L;

    private final NodeIdentifier identifier;

    private final Order order;

    private final Date date;

    public PeerStatus(final NodeIdentifier identifier, final Order order) {
        this.identifier = identifier;
        this.order = order;
        this.date = new Date();
    }

    public NodeIdentifier getIdentifier() {
        return identifier;
    }

    public Order getOrder() {
        return order;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(Object o) {
        return o instanceof PeerStatus && equals((PeerStatus) o);
    }

    public boolean equals(PeerStatus other) {
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(identifier, other.identifier);
        builder.append(date, other.date);
        builder.append(order, other.order);
        return builder.isEquals();
    }

    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(identifier);
        builder.append(date);
        builder.append(order);
        return builder.toHashCode();
    }

    /**
     * Returns <code>true</code> if the specified <code>PeerStatus</code>
     * has the same identifier and order.
     * 
     * @param other
     * @return
     */
    public boolean sameAs(PeerStatus other) {
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(identifier, other.identifier);
        builder.append(order, other.order);
        return builder.isEquals();
    }

    public Date getDate() {
        return date;
    }
}