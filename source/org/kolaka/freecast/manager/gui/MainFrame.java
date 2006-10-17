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

package org.kolaka.freecast.manager.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.apache.commons.lang.StringUtils;
import org.kolaka.freecast.auditor.AuditorFactory;
import org.kolaka.freecast.config.UserConfiguration;
import org.kolaka.freecast.lang.mutable.ObservableValue;
import org.kolaka.freecast.node.ConfigurableNode;
import org.kolaka.freecast.node.Node;
import org.kolaka.freecast.peer.Peer;
import org.kolaka.freecast.peer.PeerControler;
import org.kolaka.freecast.peer.PeerReference;
import org.kolaka.freecast.setup.SetupAction;
import org.kolaka.freecast.swing.ActionEventFactory;
import org.kolaka.freecast.swing.AsyncAction;
import org.kolaka.freecast.swing.BaseFrame;
import org.kolaka.freecast.swing.ConfigurableResources;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;
import org.kolaka.freecast.tracker.Tracker;
import org.kolaka.freecast.tracker.TrackerService;
import org.kolaka.freecast.transport.receiver.Receiver;
import org.kolaka.freecast.transport.receiver.Receiver.Source;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class MainFrame extends BaseFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5807144656289901506L;

	private final TrackerService tracker;

	private final Node node;

	private final Action visitAction;

	private final Action emailHomepageAction;

	private final Action setupAction;

	public MainFrame(Resources resources, TrackerService tracker, ConfigurableNode node,
			URL listenPage, UserConfiguration configuration) throws ResourcesException {
		super(resources);

		this.tracker = tracker;
		this.node = node;

    boolean localListenPage = tracker != null;
		visitAction = new AsyncAction(new BrowseHomepageAction(resources, listenPage, localListenPage));
		emailHomepageAction = new AsyncAction(new EmailHomepageAction(resources,
				listenPage));
		setupAction = new AsyncAction(new SetupAction(((ConfigurableResources) resources).subset("setup"), this, node, configuration));

    if (configuration.getConfiguration().isEmpty()) {
      addWindowListener(new WindowAdapter() {
        public void windowOpened(WindowEvent e) {
          ActionEvent event = new ActionEventFactory(MainFrame.this).createActionEvent();
          setupAction.actionPerformed(event);
        }
      });
    }
	}

	protected JComponent createContentPane() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weightx = 1;

    if (tracker != null) {
      panel.add(new TrackerControlPanel(tracker), constraints);
    }
		panel.add(new NodeControlPanel(node), constraints);

		return panel;
	}

	protected List createAdditionalActions() {
		Action[] actions = new Action[] { visitAction, emailHomepageAction, setupAction };
		return Arrays.asList(actions);
	}

	protected Action getButtonAction() {
		return setupAction;
	}

	abstract class ControlPanel extends JPanel {
		protected ControlPanel(String title) {
			super(new GridBagLayout());
			setOpaque(false);
			Border border = BorderFactory.createTitledBorder(BorderFactory
					.createLineBorder(Color.WHITE), " " + title + " ");
			setBorder(border);
		}

		public void add(String label, ObservableValue value) {
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(3, 3, 3, 3);

			GridBagConstraints labelConstraints = (GridBagConstraints) constraints
					.clone();
			labelConstraints.insets.left += 15;
			labelConstraints.weightx = 1;
			labelConstraints.anchor = GridBagConstraints.WEST;

			add(new JLabel(label + ":", JLabel.CENTER), labelConstraints);

			GridBagConstraints valueConstraints = (GridBagConstraints) constraints
					.clone();
			valueConstraints.gridwidth = GridBagConstraints.REMAINDER;
			valueConstraints.anchor = GridBagConstraints.EAST;

			final JLabel labelValue = new JLabel();
			labelValue.setHorizontalAlignment(JLabel.RIGHT);

			Observer observer = new Observer() {
				public void update(Observable o, Object arg) {
					ObservableValue value = (ObservableValue) o;
					String text = String.valueOf(value.getValue());
					
					String sizedText = StringUtils.abbreviate(text, text.length(), 15); 
					labelValue.setText(sizedText);
					
					String tooltip = null;
					if (sizedText.length() < text.length()) {
						tooltip = text;
					}
					labelValue.setToolTipText(tooltip);
					
					labelValue.invalidate();
				}
			};
			value.addObserver(observer);
			observer.update(value, null); // to display the current value

			add(labelValue, valueConstraints);
		}

	}

	class TrackerControlPanel extends ControlPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7865840587031612121L;

		public TrackerControlPanel(TrackerService tracker) {
			super("Tracker");

			final ObservableValue nodeCount = new ObservableValue(
					new Integer(0));

			Tracker.Auditor auditor = new Tracker.Auditor() {
				public void connectedNodes(int count) {
					nodeCount.setValue(new Integer(count));
				}

				public void register(PeerReference reference) {

				}

				public void unregister(PeerReference reference) {

				}
			};
			AuditorFactory.getInstance().register(Tracker.Auditor.class,
					auditor);

			add("Node count", nodeCount);
		}

	}

	class NodeControlPanel extends ControlPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7791975666622103751L;

		private static final String UNCONNECTED = "waiting ..";
		
		public NodeControlPanel(Node node) {
			super("Root Node");

			final ObservableValue sourceReceiver = new ObservableValue(UNCONNECTED);
			
			Receiver.Auditor receiverAuditor = new Receiver.Auditor() {
				public void receive(Source source) {
					String sourceDescription = source.getDescription();
					sourceReceiver.setValue(sourceDescription);
				}
				public void disconnected() {
					sourceReceiver.setValue(UNCONNECTED);
				}
			};
			AuditorFactory.getInstance().register(Receiver.Auditor.class, receiverAuditor);
			
			add("Receiving from", sourceReceiver);

			final ObservableValue connectedPeers = new ObservableValue(
					new Integer(0));

			PeerControler.Auditor peerAuditor = new PeerControler.Auditor() {
				public void acceptConnection(Peer peer) {

				}

				public void closeConnection(Peer peer) {

				}

				public void connectionCount(int count) {
					connectedPeers.setValue(new Integer(count));
				}
			};
			AuditorFactory.getInstance().register(PeerControler.Auditor.class,
					peerAuditor);

			add("Connected peers", connectedPeers);
			
		}

	}

}
