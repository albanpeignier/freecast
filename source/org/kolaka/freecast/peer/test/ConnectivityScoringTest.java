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

package org.kolaka.freecast.peer.test;

import junit.framework.TestCase;

import org.kolaka.freecast.peer.ConnectivityScoring;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ConnectivityScoringTest extends TestCase {

    public void testEquals() {
        assertEquals(ConnectivityScoring.UNKNOWN, ConnectivityScoring.UNKNOWN);
    }

    public void testCompareTo() {
        assertLower(ConnectivityScoring.UNKNOWN, ConnectivityScoring.MAXIMUM);
        assertLower(ConnectivityScoring.MINIMUM, ConnectivityScoring.UNKNOWN);
        assertLower(ConnectivityScoring.MINIMUM, ConnectivityScoring.MAXIMUM);
    }

    protected void assertLower(ConnectivityScoring scoring1,
            ConnectivityScoring scoring2) {
        assertTrue(scoring1 + "should be lower than " + scoring2, scoring1
                .compareTo(scoring2) < 0);
    }

    public void testBonus() {
        ConnectivityScoring scoring = ConnectivityScoring.UNKNOWN;
        ConnectivityScoring bonusScoring = ConnectivityScoring.BONUS_CONNECTIONOPENED
                .change(scoring);
        assertLower(scoring, bonusScoring);
    }

    public void testMalus() {
        ConnectivityScoring scoring = ConnectivityScoring.UNKNOWN;
        ConnectivityScoring malusScoring = ConnectivityScoring.MALUS_CONNECTIONERROR
                .change(scoring);
        assertLower(malusScoring, scoring);
    }

}