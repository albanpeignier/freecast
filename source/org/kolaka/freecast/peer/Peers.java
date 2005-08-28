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

import java.util.Comparator;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class Peers {

    private static final Transformer ORDER_TRANSFORMER = new Transformer() {
        public Object transform(Object input) {
            return ((Peer) input).getOrder();
        }
    };

    private static final Comparator ORDER_COMPARATOR = new TransformingComparator(
            ORDER_TRANSFORMER);

    public static Comparator compareOrder() {
        return ORDER_COMPARATOR;
    }

    private static final Transformer CONNECTIVITYSCORING_TRANSFORMER = new Transformer() {
        public Object transform(Object input) {
            return ((Peer) input).getConnectivityScoring();
        }
    };

    private static final Comparator CONNECTIVITYSCORING_COMPARATOR = new TransformingComparator(
            CONNECTIVITYSCORING_TRANSFORMER);

    public static Comparator compareConnectivityScoring() {
        return CONNECTIVITYSCORING_COMPARATOR;
    }

}