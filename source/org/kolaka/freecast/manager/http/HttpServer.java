/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2005 Alban Peignier
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
package org.kolaka.freecast.manager.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.service.ControlException;
import org.kolaka.freecast.service.Startable;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHttpContext;
import org.mortbay.util.Resource;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class HttpServer implements Startable {

	private InetSocketAddress listenAddress;

	private Server server;

	private InetAddress serverName;

	public HttpServer(InetSocketAddress listenAddress) {
		this.listenAddress = listenAddress;
	}

	public void start() throws ControlException {
		server = new Server();
		SocketListener listener = new SocketListener();
		listener.setInetAddress(listenAddress.getAddress());
		listener.setPort(listenAddress.getPort());
		server.addListener(listener);

		String dataResourceName = ClassUtils.getPackageName(getClass())
				.replace('.', '/')
				+ "/resources/data";
		Resource dataResource = null;
		try {
			dataResource = Resource.newSystemResource(dataResourceName);
		} catch (IOException e) {
			throw new ControlException("Can't find http server resources ("
					+ dataResourceName + ")", e);
		}
		HttpContext imagesContext = server.getContext("/data");
		imagesContext.addHandler(new ResourceHandler());
		imagesContext.setBaseResource(dataResource);

		ServletHttpContext context = (ServletHttpContext) server
				.getContext("/");

		try {
			context.addServlet("Descriptor", "/descriptor.xml",
					DescriptorServlet.class.getName());
			context.addServlet("Config", "/config.xml",
					ConfigurationServlet.class.getName());
			ServletHolder holder = context.addServlet("Home", "/",
					HomeServlet.class.getName());
			holder.setInitParameter("servername", serverName.getHostAddress());
		} catch (Exception e) {
			throw new ControlException("Can't install the descriptor servlet",
					e);
		}

		LogFactory.getLog(getClass()).info(
				"start http server on " + listenAddress);

		try {
			server.start();
		} catch (Exception e) {
			throw new ControlException("Can't start the http server", e);
		}
	}

	public void stop() throws ControlException {
		try {
			server.stop();
		} catch (InterruptedException e) {
			throw new ControlException("Can't stop the http server", e);
		}
	}

	public void setServerName(InetAddress serverName) {
		this.serverName = serverName;
	}
}
