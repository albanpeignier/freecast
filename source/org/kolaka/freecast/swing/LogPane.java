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

package org.kolaka.freecast.swing;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.TTCCLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class LogPane extends JPanel {

	private static final long serialVersionUID = 3904965256890298932L;

	private TableModel model;

	public LogPane() {
		setLayout(new GridBagLayout());

		model = new TableModel();
		JTable table = new JTable(model);
		table.getColumnModel().getColumn(0).setCellRenderer(
				new LevelCellRenderer());
		table.getColumnModel().getColumn(0).setMinWidth(20);
		table.getColumnModel().getColumn(0).setPreferredWidth(20);
		table.getColumnModel().getColumn(0).setMaxWidth(80);

		table.setRowHeight(20);

		GridBagConstraints tableConstraints = new GridBagConstraints();
		tableConstraints.fill = GridBagConstraints.BOTH;
		tableConstraints.weightx = tableConstraints.weighty = 1.0;
		tableConstraints.gridwidth = GridBagConstraints.REMAINDER;

		add(new JScrollPane(table), tableConstraints);

		GridBagConstraints buttonsConstraints = new GridBagConstraints();
		buttonsConstraints.anchor = GridBagConstraints.EAST;

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(new JButton(new SaveAction()));
		buttonPanel.add(new JButton(new EmailAction()));
		add(buttonPanel, buttonsConstraints);
	}

	public Appender getAppender() {
		return model.getAppender();
	}

	class LevelCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 3762536710978090805L;

		private Map icons = new HashMap();

		LevelCellRenderer() {
			load(Level.INFO, "resources/log-info.png");
			load(Level.WARN, "resources/log-warn.png");
			load(Level.ERROR, "resources/log-error.png");
			load(Level.FATAL, "resources/log-fatal.png");
		}

		private final Icon DEFAULT_ICON = new ImageIcon(getClass().getResource(
				"resources/log.png"));

		private void load(Level level, String resource) {
			icons.put(level, new ImageIcon(getClass().getResource(resource)));
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);
			Icon icon = (Icon) icons.get(value);
			if (icon == null) {
				icon = DEFAULT_ICON;
			}
			label.setIcon(icon);
			return label;
		}

	}

	class TableModel extends AbstractTableModel {
		private static final long serialVersionUID = 3905521614119842613L;

		private final AppenderImpl appender = new AppenderImpl();

		private final List events = new ArrayList();

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return events.size();
		}

		public Object getValueAt(int row, int col) {
			LoggingEvent event = (LoggingEvent) events.get(row);
			switch (col) {
			case 0:
				return event.getLevel();
			/*
			 * case 1: String loggerName = event.getLoggerName(); return
			 * loggerName.substring(loggerName.lastIndexOf('.') + 1);
			 */
			case 1:
				return event.getMessage();
			default:
				throw new IllegalArgumentException("Invalid column index: "
						+ col);
			}
		}

		class AppenderImpl extends AppenderSkeleton {

			private static final int MAX_EVENTCOUNT = 1000;

			protected void append(LoggingEvent event) {
				events.add(0, event);
				fireTableRowsInserted(0, 0);

				while (events.size() > MAX_EVENTCOUNT) {
					int lastIndex = events.size() - 1;
					events.remove(lastIndex);
					fireTableRowsDeleted(lastIndex, lastIndex);
				}
			}

			public void close() {
				events.clear();
			}

			public boolean requiresLayout() {
				return false;
			}

		}

		public Appender getAppender() {
			return appender;
		}

		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Level";
			/*
			 * case 1: return "Category";
			 */
			case 1:
				return "Message";
			default:
				throw new IllegalArgumentException("Invalid column index: "
						+ col);
			}
		}

		public List getLoggingEvents() {
			return new LinkedList(events);
		}

	}

	private static final Icon SAVE_ICON = new ImageIcon(SaveAction.class
			.getResource("resources/log-save.png"));

	private static final Icon EMAIL_ICON = new ImageIcon(SaveAction.class
			.getResource("resources/log-email.png"));

	class SaveAction extends BaseAction {

		private static final long serialVersionUID = 3688502216508258613L;

		public SaveAction() {
			super("Save Log", SAVE_ICON);
		}

		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser();
			if (chooser.showSaveDialog(LogPane.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File file = chooser.getSelectedFile();
			save(file);
			JOptionPane.showMessageDialog(LogPane.this,
					"FreeCast log has been into\n" + file.getAbsolutePath());
		}

	}

	class EmailAction extends BaseAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3256728372691155254L;

		public EmailAction() {
			super("Mail Log", EMAIL_ICON);
		}

		public void actionPerformed(ActionEvent event) {
			try {
				File file = File.createTempFile("freecast", ".log");
				save(file);

				Message message = new Message();
				message.setToAddrs(Collections
						.singletonList("freecast-support@lists.tryphon.org"));
				message.setSubject("FreeCast log report");
				message.setBody("FreeCast network:\nProblem description:\n");

				message.setAttachments(Collections.singletonList(file
						.getAbsolutePath()));

				Desktop.mail(message);
			} catch (Exception e) {
				LogFactory.getLog(getClass()).error(
						"can't prepare a log email", e);
			}
		}

	}

	private final Layout layout = new TTCCLayout("DATE");

	private void save(File file) {
		List events = model.getLoggingEvents();
		Collections.reverse(events);

		LogFactory.getLog(getClass()).debug("save log to " + file);

		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(file)));
			for (Iterator iter = events.iterator(); iter.hasNext();) {
				LoggingEvent event = (LoggingEvent) iter.next();
				writer.print(layout.format(event));

				ThrowableInformation throwableInformation = event
						.getThrowableInformation();
				if (throwableInformation != null) {
					throwableInformation.getThrowable().printStackTrace(writer);
				}
			}
			writer.close();
		} catch (IOException e) {
			LogFactory.getLog(getClass()).error("save log failed", e);
		}
	}

}