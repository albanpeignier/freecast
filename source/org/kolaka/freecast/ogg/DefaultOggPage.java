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

package org.kolaka.freecast.ogg;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultOggPage implements OggPage, MutableOggPage {

    private boolean firstPage;

    private boolean lastPage;
    
    private long absoluteGranulePosition;
    
    private int streamSerialNumber;

    private byte[] bytes;

    public byte[] getRawBytes() {
        if (bytes == null) {
            throw new IllegalStateException("No defined bytes");
        }
        return bytes;
    }

    public void setRawBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public boolean isFirstPage() {
        return firstPage;
    }

    public void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }
    
    public long getAbsoluteGranulePosition() {
        return absoluteGranulePosition;
    }

    public void setAbsoluteGranulePosition(long absoluteGranulePosition) {
        this.absoluteGranulePosition = absoluteGranulePosition;
    }
    
    public int getStreamSerialNumber() {
        return streamSerialNumber;
    }
    
    public String getStreamSerialNumberString() {
        return Integer.toHexString(streamSerialNumber);
    }

    public void setStreamSerialNumber(int streamSerialNumber) {
        this.streamSerialNumber = streamSerialNumber;
    }
    
    public int getLength() {
        return bytes != null ? bytes.length : 0;
    }

    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("firstpage", firstPage);
        builder.append("lastpage", lastPage);
        builder.append("streamSerialNumber", getStreamSerialNumberString());
        builder.append("absoluteGranulePosition", absoluteGranulePosition);
        return builder.toString();
    }
    
}