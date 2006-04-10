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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class ToolbarPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3197243213538241226L;

	private final JPanel buttonsPanel;

	private Action messageAction = NullAction.getInstance();

	public void setMessageAction(Action messageAction) {
		Validate.notNull(messageAction, "No specified message action");
		this.messageAction = messageAction;
	}

	public void add(Action action) {
		IconButton button = new IconButton(action);
		buttonsPanel.add(button);
	}

	public ToolbarPane() {
		super(new GridBagLayout());
		setOpaque(false);

		buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.setOpaque(false);
		GridBagConstraints buttonsConstraints = new GridBagConstraints();
		buttonsConstraints.weightx = 0.0;
		add(buttonsPanel, buttonsConstraints);

		createMessageLabel();

		GridBagConstraints emptyConstraints = new GridBagConstraints();
		emptyConstraints.gridwidth = GridBagConstraints.REMAINDER;
		emptyConstraints.weightx = 1.0;
		emptyConstraints.fill = GridBagConstraints.HORIZONTAL;

		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		add(emptyPanel, emptyConstraints);
	}

	private void createMessageLabel() {
		final JLabel messageLabel = new JLabel("Welcome to FreeCast !");

		GridBagConstraints messageConstraints = new GridBagConstraints();
		messageConstraints.weightx = 0.0;
		messageConstraints.insets = new Insets(0, 5, 0, 0);
		add(messageLabel, messageConstraints);

		messageLabel.addMouseListener(new MouseAdapter() {
			private ActionEventFactory factory = new ActionEventFactory(ToolbarPane.this);
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					ActionEvent actionEvent = factory.createActionEvent();
					messageAction.actionPerformed(actionEvent);
				}
			}

		});
		Appender messageAppender = new AppenderSkeleton() {
			protected void append(LoggingEvent event) {
				Level level = event.getLevel();
				if (level.isGreaterOrEqual(Level.INFO)) {
					String truncatedMessage = StringUtils.abbreviate(String
							.valueOf(event.getMessage()), 35);
					messageLabel.setText(truncatedMessage);
				}
			}

			public void close() {

			}

			public boolean requiresLayout() {
				return false;
			}

		};
		Logger.getRootLogger().addAppender(messageAppender);
	}

}
