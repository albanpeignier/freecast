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

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Startable;
import org.kolaka.freecast.timer.DefaultTimer;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class OggSourceFactoryReceiver extends OggSourceReceiver {

    private OggSourceFactory factory;
    
    public OggSourceFactoryReceiver(OggSourceFactory factory) {
        this.factory = factory;
    }

    public void start() throws ControlException {
        if (factory instanceof Startable) {
            ((Startable) factory).start();
        }
        super.start();
    }

    public void stop() throws ControlException {
        if (factory instanceof Startable) {
            ((Startable) factory).stop();
        }
        super.stop();
    }
    
    protected Loop createLoop() {
        return new Loop() {
            
            public long loop() throws LoopInterruptedException {
                OggSource oggSource;
                
                try {
                    oggSource = factory.next();
                } catch (IOException e) {
                    String message = "Can't create next OggSource with " + factory + ", wait 10 secondes before retrying";
                    LogFactory.getLog(getClass()).error(message,e);
                    return DefaultTimer.seconds(30);
                }

                try {
                    receive(oggSource);
                } catch (IOException e) {
                    LogFactory.getLog(getClass()).error("Stream reception failed with  " + oggSource, e);
                    return DefaultTimer.seconds(3);
                }

                return DefaultTimer.nodelay();
            }
        };
    }
    
}
