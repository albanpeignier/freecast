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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Loop;
import org.kolaka.freecast.timer.LoopInterruptedException;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class HttpPlayerSource extends BasePlayerSource implements TimerUser {

    private InetSocketAddress listenAddress;
    private ServerSocket serverSocket;
    private boolean stopped;

    public HttpPlayerSource(int port) {
        this(new InetSocketAddress(port));
    }
    
    public HttpPlayerSource(InetSocketAddress listenAddress) {
        Validate.notNull(listenAddress, "No specified listen address");
        this.listenAddress = listenAddress;
    }

    private Loop clientAcception = new Loop() {

        protected long loop() throws LoopInterruptedException {
            LogFactory.getLog(getClass()).info("wait for http player connection at " + listenAddress);
            
            Socket socket;

            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                if (stopped) {
                    LogFactory.getLog(getClass()).debug(
                            "socket acceptation stopped", e);
                    return DefaultTimer.nodelay();
                }

                LogFactory.getLog(getClass()).error(
                        "socket acceptation failed", e);
                return DefaultTimer.seconds(5);
            }
            
            LogFactory.getLog(getClass()).info("accepted http player from " + socket.getInetAddress().getHostName());
            
            HttpPlayer player = new HttpPlayer(socket);
            processPlayerCreated(player);
            
            return DefaultTimer.nodelay();
        }
    };
        
    public void start() throws ControlException {
        stopped = false;
        
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(listenAddress);
        } catch (IOException e) {
            throw new ControlException("Can't wait http connections on " + listenAddress, e);
        }
        
        timer.execute(clientAcception);
        
        super.start();
    }


    public void stop() throws ControlException {
        stopped = true;
        clientAcception.cancel();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new ControlException("Can't close http server socket", e);
            }
        }
    }

    private Timer timer = DefaultTimer.getInstance();

    public void setTimer(Timer timer) {
        Validate.notNull(timer, "No specified Timer");
        this.timer = timer;
    }

}
