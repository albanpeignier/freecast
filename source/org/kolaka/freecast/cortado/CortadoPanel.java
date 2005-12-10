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

package org.kolaka.freecast.cortado;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class CortadoPanel extends JPanel {

	private static final long serialVersionUID = 3618137831873262640L;

	private CortadoApplet applet;

	public CortadoPanel() {
		super(new BorderLayout());

		setPreferredSize(new Dimension(384, 290));

		applet = new CortadoApplet();
		applet.setParam("preBuffer", "false");
		add(applet, BorderLayout.CENTER);

		setAudio(false);
		setFrameRate(30);
	}

	public void setAudio(boolean audio) {
		LogFactory.getLog(getClass()).debug("set audio to " + audio);
		setParam("audio", String.valueOf(audio));
	}

	public void setFrameRate(int frameRate) {
		LogFactory.getLog(getClass()).debug("set framerate to " + frameRate);
		setParam("framerate", String.valueOf(frameRate));
	}

	private void setParam(String name, String value) {
		applet.setParam(name, value);
	}

	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (applet != null) {
			applet.setBackground(bg);
		}
	}

	public void start() {
		applet.init();
		applet.start();
	}

	public void setStream(URL streamURL) {
		applet.setParam("url", streamURL.toExternalForm());
	}

	public void setStream(InputStream inputstream) {
		applet.setStream(inputstream);
	}

	public void stop() {
		applet.stop();
	}

	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		URL streamURL = null;

		if (!file.exists()) {
			streamURL = new URL(args[0]);
			file = null;
		}

		JFrame frame = new JFrame();
		CortadoPanel panel = new CortadoPanel();

		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);

		if (file != null) {
			System.out.println("open file " + file);
			panel.setStream(new FileInputStream(file));
		} else {
			panel.setStream(streamURL);
		}

		panel.start();
	}

}
