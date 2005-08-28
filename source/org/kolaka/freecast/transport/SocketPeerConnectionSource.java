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

package org.kolaka.freecast.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;
import org.kolaka.freecast.peer.PeerConnection;
import org.kolaka.freecast.peer.PeerConnectionSource;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.timer.DefaultTimer;
import org.kolaka.freecast.timer.Loop;
import org.kolaka.freecast.timer.Task;
import org.kolaka.freecast.timer.Timer;
import org.kolaka.freecast.timer.TimerUser;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class SocketPeerConnectionSource extends PeerConnectionSource implements
        TimerUser {

    private final InetSocketAddress address;

    private ServerSocket serverSocket;

    private Loop clientAcceptationTask;

    private ServerSocketFactory factory = ServerSocketFactory.getDefault();

    public SocketPeerConnectionSource(InetSocketAddress address) {
        this.address = address;
    }

    public void start() throws ControlException {
        stopped = false;
        try {
            serverSocket = factory.createServerSocket();
            serverSocket.bind(address);
        } catch (IOException e) {
            throw new ControlException("Can't initialize the ServerSocket on "
                    + address, e);
        }

        LogFactory.getLog(getClass()).debug("wait connections on " + address);

        clientAcceptationTask = new ClientAcceptationTask();
        timer.execute(clientAcceptationTask);
    }

    private boolean stopped;

    public void stop() throws ControlException {
        stopped = true;

        if (clientAcceptationTask != null) {
            clientAcceptationTask.cancel();
            clientAcceptationTask = null;
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new ControlException("Can't close the ServerSocket", e);
            }
        }
    }

    public void setServerSocketFactory(ServerSocketFactory factory) {
        Validate.notNull(factory, "No specified ServerSocketFactory");
        this.factory = factory;
    }

    class ClientAcceptationTask extends Loop {

        protected long loop() {
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

            LogFactory.getLog(getClass()).trace(
                    "new client socket connection: " + socket.getInetAddress());

            SocketPeerConnection connection;

            try {
				socket.setSendBufferSize(1024);
                connection = new SocketPeerConnection(
                        PeerConnection.Type.RELAY, socket);
            } catch (IOException e) {
                LogFactory.getLog(getClass()).error(
                        "socket acceptation failed", e);
                return DefaultTimer.nodelay();
            }

            timer.executeLater(new ConnectionAcceptation(connection));

            return DefaultTimer.nodelay();
        }

    }

    /**
     * @todo move toan upper class
     * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
     */
    class ConnectionAcceptation extends Task {

        private final PeerConnection connection;

        public ConnectionAcceptation(PeerConnection connection) {
            this.connection = connection;
        }

        public void run() {
            NDC.push(connection.toString());
            try {
                accept(connection);
            } finally {
                NDC.pop();
            }
        }

    }

    /**
     * @todo move toan upper class
     */
    private Timer timer = DefaultTimer.getInstance();

    public void setTimer(Timer timer) {
        Validate.notNull(timer, "No specified Timer");
        this.timer = timer;
    }
}
