/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2006 Alban Peignier
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

package org.kolaka.freecast.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.kolaka.freecast.Version;

/**
 * @author alban
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 3257002168149094451L;

	public AboutDialog(Resources resources, JFrame parent)
			throws ResourcesException {
		super(parent, parent.getTitle() + " - About");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());

		contentPane.setBackground(resources.getColor("background"));

		JLabel logoLabel = new JLabel(resources.getIcon("logo"));

		GridBagConstraints logoConstraints = new GridBagConstraints();
		logoConstraints.insets = new Insets(10, 10, 10, 10);
		logoConstraints.gridwidth = GridBagConstraints.REMAINDER;
		contentPane.add(logoLabel, logoConstraints);

		String urlText = "<html><center><p>Visit the website<br><b>http://www.freecast.org</b></p>&nbsp;"
				+ "<p>Free software under the GNU GPL<br>version "
				+ Version.getINSTANCE().getName()
				+ "<br>Copyright 2004 - Alban Peignier</p></center>";
		JLabel urlLabel = new JLabel(urlText);
		GridBagConstraints urlConstraints = new GridBagConstraints();
		urlConstraints.gridwidth = GridBagConstraints.REMAINDER;
		urlConstraints.insets = new Insets(0, 5, 10, 5);
		contentPane.add(urlLabel, urlConstraints);

		urlLabel.setForeground(Color.WHITE);

		setLocationRelativeTo(parent);
		pack();
	}

}