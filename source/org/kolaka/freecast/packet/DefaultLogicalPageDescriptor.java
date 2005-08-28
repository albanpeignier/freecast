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

package org.kolaka.freecast.packet;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class DefaultLogicalPageDescriptor implements LogicalPageDescriptor {

    private final long sequenceNumber;
    private final int count;
    private final boolean firstPage;

    public DefaultLogicalPageDescriptor(long sequenceNumber, int count, boolean firstPage) {
        this.sequenceNumber = sequenceNumber;
        this.count = count;
        this.firstPage = firstPage;
    }

    public int getCount() {
        return count;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }
    
    public boolean isFirstPage() {
        return firstPage;
    }

    public LogicalPageDescriptor.Element createElementDescriptor(final int index) {
        return new LogicalPageDescriptor.Element() {
            public LogicalPageDescriptor getPageDescriptor() {
                return DefaultLogicalPageDescriptor.this;
            }
            public int getIndex() {
                return index;
            }
        };
    }
    
    public boolean equals(Object o) {
        return o instanceof LogicalPageDescriptor && equals((LogicalPageDescriptor) o);
    }
    
    public boolean equals(LogicalPageDescriptor other) {
        return other != null && sequenceNumber == other.getSequenceNumber();
    }
    
    public int hashCode() {
        return (int) sequenceNumber;
    }
    
}
