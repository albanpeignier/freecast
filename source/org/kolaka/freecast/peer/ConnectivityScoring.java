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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ConnectivityScoring implements Comparable, Serializable {

    private static final long serialVersionUID = 3834586617058178101L;

    private int value;

    private static int MINIMUM_VALUE = -50;

    private static int MAXIMUM_VALUE = 50;

    private static int WIDTH = MAXIMUM_VALUE - MINIMUM_VALUE;

    public static ConnectivityScoring MINIMUM = new ConnectivityScoring(
            MINIMUM_VALUE);

    public static ConnectivityScoring MAXIMUM = new ConnectivityScoring(
            MAXIMUM_VALUE);

    public static ConnectivityScoring UNKNOWN = new ConnectivityScoring(0);

    /*
     * @todo these constants could be defined by the Peer or the PeerControler
     */
    public static ConnectivityScoring UNREACHEABLE = new ConnectivityScoring(
            (int) (MINIMUM_VALUE + WIDTH * 0.1));

    public static final Change BONUS_CONNECTIONOPENED = new DeltaChange(
            "connection opened", 20);

    public static final Change MALUS_CONNECTIONCLOSED = new DeltaChange(
            "connection error", -20);

    public static final Change MALUS_CONNECTIONERROR = new DeltaChange(
            "connection error", -30);

    public static final Change BONUS_CONNECTIONTRAFFIC = new DeltaChange(
            "connection traffic", 5);

    public static final Change IDLE = new IdleChange();

    private ConnectivityScoring(int value) {
        this.value = Math.min(Math.max(value, MINIMUM_VALUE), MAXIMUM_VALUE);
    }

    int getValue() {
        return value;
    }

    public int compareTo(Object o) {
        return value - ((ConnectivityScoring) o).value;
    }

    public String toString() {
        return "CS[" + value + "]";
    }

    public int hashCode() {
        return value;
    }

    public boolean equals(Object o) {
        return o instanceof ConnectivityScoring
                && equals((ConnectivityScoring) o);
    }

    public boolean equals(ConnectivityScoring other) {
        return other != null && value == other.value;
    }

    public abstract static class Change {

        private String reason;

        public Change(String reason) {
            this.reason = reason;
        }

        public abstract ConnectivityScoring change(ConnectivityScoring scoring);

        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    public static class DeltaChange extends Change {

        private final int amount;

        private final ConnectivityScoring bound;

        public ConnectivityScoring change(ConnectivityScoring scoring) {
            if (amount == 0) {
                return scoring;
            }

            int newValue = scoring.getValue() + amount;

            if (amount < 0) {
                newValue = Math.max(newValue, bound.getValue());
            } else {
                newValue = Math.min(newValue, bound.getValue());
            }

            return new ConnectivityScoring(newValue);
        }

        public DeltaChange(final String reason, final int amount,
                final ConnectivityScoring bound) {
            super(reason);

            this.amount = amount;
            this.bound = bound;
        }

        DeltaChange(final String reason, final int amount) {
            this(reason, amount, amount > 0 ? MAXIMUM : MINIMUM);
        }

    }

    public static class IdleChange extends Change {

        IdleChange() {
            super("idle");
        }

        public ConnectivityScoring change(ConnectivityScoring scoring) {
            int comparedToUnknown = scoring.compareTo(UNKNOWN);

            if (comparedToUnknown == 0) {
                return scoring;
            }

            int newValue;

            if (comparedToUnknown > 0) {
                newValue = Math.max(UNKNOWN.getValue(), scoring.getValue() - 2);
            } else {
                newValue = Math.min(UNKNOWN.getValue(), scoring.getValue() + 5);
            }

            return new ConnectivityScoring(newValue);

        }

    }

}