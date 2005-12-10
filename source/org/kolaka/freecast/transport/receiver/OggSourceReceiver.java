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

package org.kolaka.freecast.transport.receiver;

import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.packet.LogicalPage;
import org.kolaka.freecast.packet.signer.PacketChecksummer;
import org.kolaka.freecast.pipe.Producer;
import org.kolaka.freecast.pipe.Producers;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.LoopService;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public abstract class OggSourceReceiver extends LoopService implements SourceReceiver {

    private Producer producer;
    
    public void setProducer(Producer producer) {
        this.producer = producer;
    }
    
    private OggLogicalPageFactory pageFactory = new OggLogicalPageFactory();
    private BandwidthControler bandwidthControler = new TimestampBandwidthControler();
    
    public void setPacketChecksummer(PacketChecksummer checksummer) {
        pageFactory.setPacketChecksummer(checksummer);
    }
    
    public void setBandwidthControler(BandwidthControler bandwidthControler) {
        Validate.notNull(bandwidthControler);
        this.bandwidthControler = bandwidthControler;
    }
    
    public void stop() throws ControlException {
        producer.close();
        super.stop();
    }
    
    protected void receive(OggSource source) throws IOException {
        LogFactory.getLog(getClass()).debug("start to receive Ogg stream");        
        pageFactory.setSource(source);
        
        while (true) {
            LogicalPage logicalPage = pageFactory.next();
            
            if (logicalPage.isFirstPage()) {
                LogFactory.getLog(getClass()).debug("new stream header created: " + logicalPage);        
            }
            
            long delay = bandwidthControler.getTimeDelay(logicalPage);
            if (delay > 10) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    IOException exception = new IOException("Can't make sleep the thread");
                    exception.initCause(e);
                    throw exception;
                }
            }

            Producers.pushAll(producer, logicalPage);
        }
    }
    
}
