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
package org.kolaka.freecast.manager.gui;

import org.kolaka.freecast.auditor.AuditorFactory;
import org.kolaka.freecast.lang.mutable.ObservableValue;
import org.kolaka.freecast.node.Node;
import org.kolaka.freecast.peer.PeerControler;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.swing.BaseFrame;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;
import org.kolaka.freecast.tracker.Tracker;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class MainFrame extends BaseFrame {
	private final Tracker tracker;
	private final Node node;
	private final Action visitAction;
	private final Action emailHomepageAction;

	public MainFrame(Resources resources, Tracker tracker, Node node, InetSocketAddress publicHttpServer)
	        throws ResourcesException {
		super(resources);

		this.tracker = tracker;
		this.node = node;

		visitAction = new BrowseHomepageAction(resources, publicHttpServer);
		emailHomepageAction = new EmailHomepageAction(resources, publicHttpServer);
	}

	protected JComponent createContentPane() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weightx = 1;

		panel.add(new TrackerControlPanel(tracker), constraints);
		panel.add(new NodeControlPanel(node), constraints);

		return panel;
	}

	protected List createAdditionalActions() {
		Action[] actions = new Action[] { visitAction, emailHomepageAction };
		return Arrays.asList(actions);
	}

	protected Action getButtonAction() {
		return visitAction;
	}

	abstract class ControlPanel extends JPanel {
		protected ControlPanel(String title) {
			super(new GridBagLayout());
			setOpaque(false);
			Border border =
			        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), " " + title + " ");
			setBorder(border);
		}

		public void add(String label, ObservableValue value) {
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(3, 3, 3, 3);

			GridBagConstraints labelConstraints = (GridBagConstraints) constraints.clone();
			labelConstraints.insets.left += 15;
			labelConstraints.weightx = 1;
			labelConstraints.anchor = GridBagConstraints.WEST;

			add(new JLabel(label + ":", JLabel.CENTER), labelConstraints);

			GridBagConstraints valueConstraints = (GridBagConstraints) constraints.clone();
			valueConstraints.gridwidth = GridBagConstraints.REMAINDER;
			valueConstraints.anchor = GridBagConstraints.EAST;

			final JLabel labelValue = new JLabel();
			labelValue.setHorizontalAlignment(JLabel.RIGHT);

			Observer observer = new Observer() {
				public void update(Observable o, Object arg) {
					ObservableValue value = (ObservableValue) o;
					labelValue.setText(String.valueOf(value.getValue()));
					labelValue.invalidate();
				}
			};
			value.addObserver(observer);
			observer.update(value, null); // to display the current value

			add(labelValue, valueConstraints);
		}

	}

	class TrackerControlPanel extends ControlPanel {
		private final Tracker tracker;

		public TrackerControlPanel(Tracker tracker) {
			super("Tracker");
			this.tracker = tracker;

			final ObservableValue nodeCount = new ObservableValue(new Integer(0));

			Tracker.Auditor auditor = new Tracker.Auditor() {
				public void connectedNodes(int count) {
					nodeCount.setValue(new Integer(count));
				}

				public void register(PeerReference reference) {

				}

				public void unregister(PeerReference reference) {

				}
			};
			AuditorFactory.getInstance().register(Tracker.Auditor.class, auditor);

			add("Node count", nodeCount);
		}

	}

	class NodeControlPanel extends ControlPanel {
		private final Node node;

		public NodeControlPanel(Node node) {
			super("Root Node");
			this.node = node;

			final ObservableValue connectedPeers = new ObservableValue(new Integer(0));

			PeerControler.Auditor auditor = new PeerControler.Auditor() {
				public void acceptConnection(PeerReference reference) {

				}

				public void closeConnection(PeerReference reference) {

				}

				public void connectionCount(int count) {
					connectedPeers.setValue(new Integer(count));
				}
			};
			AuditorFactory.getInstance().register(PeerControler.Auditor.class, auditor);

			add("Connected peers", connectedPeers);
		}

	}

}
