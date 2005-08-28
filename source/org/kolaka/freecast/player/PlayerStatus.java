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

package org.kolaka.freecast.player;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class PlayerStatus implements Serializable {

    private static final long serialVersionUID = 3688502199378785336L;

    public static final PlayerStatus INACTIVE = new PlayerStatus(0, 0);

    private long playTimeLength;

    private double missingDataRate;

    public PlayerStatus(final long playTimeLength, final double missingDataRate) {
        this.playTimeLength = playTimeLength;
        this.missingDataRate = missingDataRate;
    }

    public double getMissingDataRate() {
        return missingDataRate;
    }

    public long getPlayTimeLength() {
        return playTimeLength;
    }

    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("playTimeLength", DateFormatUtils.formatUTC(
                playTimeLength, "HH:mm:ss"));
        builder.append("missingDataRate", missingDataRate);
        return builder.toString();
    }

}