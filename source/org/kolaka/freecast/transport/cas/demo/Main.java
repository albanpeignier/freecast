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

package org.kolaka.freecast.transport.cas.demo;



public class Main {

	/*
	private static final int SERVICE_PORT = 30050;

	private static final InetSocketAddressParser ADDRESS_PARSER = new InetSocketAddressParser();

	public static void main(String args[]) throws Exception {
		CommandLine cl = createCommandLine(args);

		InetAddress localhostAddress = InetAddress.getLocalHost();

		InetSocketAddress serviceAddress = new InetSocketAddress(
				"www.tryphon.org", SERVICE_PORT);
		if (cl.hasOption("-server")) {
			serviceAddress = ADDRESS_PARSER.parse((String) cl
					.getValue("-server"));
		}

		InetSocketAddress targetAddress = null;
		if (cl.hasOption("-target")) {
			targetAddress = ADDRESS_PARSER.parse((String) cl
					.getValue("-target"));
		}

		InetAddress publicAddress = localhostAddress;
		if (cl.hasOption("-public")) {
			publicAddress = InetAddress.getByName((String) cl
					.getValue("-public"));
		}

		IoConnector connector = new SocketConnector();
		ConnectionAssistantService service = new ConnectionAssistantServiceStub(
				connector, serviceAddress);

		final Session session = service.connect();
		int listenPort = 30051; // (int) (30000 + Math.random() * 1000);
		if (cl.hasOption("-port")) {
			listenPort = Integer.parseInt((String) cl.getValue("-port"));
		}

		LogFactory.getLog(Main.class).info("wait connection on " + listenPort);
		ConnectionHandler handler = listen(listenPort);

		InetSocketAddress listenAddress = new InetSocketAddress(publicAddress,
				listenPort);
		LogFactory.getLog(Main.class).info(
				"register listenAddress " + listenAddress);
		session.register(listenAddress, handler);

		if (targetAddress != null) {
			connect(session, targetAddress, publicAddress);
		}

		Thread thread = new Thread() {
			public void run() {
				try {
					session.close();
				} catch (Exception e) {
					LogFactory.getLog(Main.class).error(
							"Can't close the session", e);
				}
			};
		};
		Runtime.getRuntime().addShutdownHook(thread);

		final Object lock = new Object();
		synchronized (lock) {
			lock.wait();
		}
	}

	private static CommandLine createCommandLine(String[] args)
			throws OptionException {
		final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		final ArgumentBuilder abuilder = new ArgumentBuilder();
		final GroupBuilder gbuilder = new GroupBuilder();

		Option server = obuilder.withShortName("server").withShortName("s")
				.withDescription("ConnectionAssistantService to be used")
				.withArgument(
						abuilder.withName("host:port").withMinimum(1)
								.withMaximum(1).create()).create();

		Option port = obuilder.withShortName("port").withShortName("p")
				.withDescription("Port where the connections are waited")
				.withArgument(
						abuilder.withName("port").withMinimum(1).withMaximum(1)
								.create()).create();

		Option target = obuilder.withShortName("target").withShortName("t")
				.withDescription("the remote peer to be connected")
				.withArgument(
						abuilder.withName("host:port").withMinimum(1)
								.withMaximum(1).create()).create();

		Option publicAddress = obuilder.withShortName("public")
				.withDescription("the public address of this peer")
				.withArgument(
						abuilder.withName("host").withMinimum(1).withMaximum(1)
								.create()).create();

		Group options = gbuilder.withName("options").withOption(server)
				.withOption(target).withOption(port).withOption(publicAddress)
				.create();

		Parser parser = new Parser();
		parser.setGroup(options);
		CommandLine cl = parser.parse(args);
		return cl;
	}

	private static ConnectionHandler listen(int listenPort)
			throws SocketException {
		final DatagramSocket socket = new DatagramSocket(listenPort);

		ConnectionHandler handler = new ConnectionHandler() {
			public void connectionRequested(InetSocketAddress sourceAddress,
					InetSocketAddress targetAddress) {
				LogFactory.getLog(Main.class).debug(
						"connection requested from " + sourceAddress);
				DatagramPacket packet = new DatagramPacket(new byte[] { 'b' },
						1);
				packet.setSocketAddress(sourceAddress);
				try {
					socket.send(packet);
				} catch (IOException e) {
					LogFactory.getLog(Main.class)
							.error(
									"can't send a fake responde to "
											+ sourceAddress, e);
				}
			}
		};

		Runnable runnable = new Runnable() {
			public void run() {
				DatagramPacket packet = new DatagramPacket(new byte[1], 1);
				try {
					socket.receive(packet);
					LogFactory.getLog(Main.class).info(
							"receive packet from " + packet.getAddress());
					socket.send(packet);
				} catch (IOException e) {
					LogFactory.getLog(Main.class).error(
							"failed to process datagram packet", e);
				}
			}
		};
		new Thread(runnable).start();
		return handler;
	}

	private static void connect(Session session,
			InetSocketAddress targetAddress, InetAddress publicAddress)
			throws Exception {
		int listenPort = (int) (40000 + Math.random() * 10000);
		DatagramSocket socket = new DatagramSocket(listenPort);

		LogFactory.getLog(Main.class).info(
				"request assistance to connect " + targetAddress);
		session.assist(targetAddress, new InetSocketAddress(publicAddress,
				listenPort));

		LogFactory.getLog(Main.class).info("connect to " + targetAddress);
		socket.connect(targetAddress);

		for (int i = 0; i < 3; i++) {
			LogFactory.getLog(Main.class).info(
					"send packet to " + targetAddress);
			DatagramPacket packet = new DatagramPacket(new byte[] { 'a' }, 1);
			socket.send(packet);
		}

		LogFactory.getLog(Main.class).info(
				"wait response from " + targetAddress);
		DatagramPacket packet = new DatagramPacket(new byte[1], 1);
		socket.receive(packet);

		LogFactory.getLog(Main.class).info(
				"received response from " + targetAddress);
	}
	*/
	
}
