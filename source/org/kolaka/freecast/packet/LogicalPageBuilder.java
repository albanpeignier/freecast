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

import org.apache.commons.lang.Validate;
import org.kolaka.freecast.collections.SortedList;

import java.util.List;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class LogicalPageBuilder {

    private LogicalPageDescriptor descriptor;
    private final List packets;

	public LogicalPageBuilder(LogicalPageDescriptor descriptor) {
        Validate.notNull(descriptor, "No specified LogicalPageDescriptor");
        this.descriptor = descriptor;
        this.packets = new SortedList(Packets.compareElementIndex());
    }

    public LogicalPageBuilder(long sequenceNumber, int packetCount, boolean firstPage) {
        this(new DefaultLogicalPageDescriptor(sequenceNumber, packetCount, firstPage));
    }

    public void add(Packet packet) {
        packets.add(packet);
    }
    
    public boolean isComplete() {
        return packets.size() == descriptor.getCount();
    }

	public LogicalPage create() {
        if (!isComplete()) {
            throw new IllegalStateException("incomplete packet list: " + this);
        }
        return new DefaultLogicalPage(descriptor, packets);
    }
    
    public LogicalPageDescriptor.Element createElementDescriptor(final int index) {
        return new LogicalPageDescriptor.Element() {
            public LogicalPageDescriptor getPageDescriptor() {
                return descriptor;
            }
            public int getIndex() {
                return index;
            }
        };
    }
    
}
