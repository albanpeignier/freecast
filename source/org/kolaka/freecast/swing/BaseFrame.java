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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class BaseFrame extends JFrame {

	private final Resources resources;

	private Action aboutAction, logAction, quitAction, showHideMainAction,
			showHideToolbarAction;

	private static final int GAP = 10;

	public BaseFrame(Resources resources) {
		this.resources = resources;
	}

	protected Resources getResources() {
		return resources;
	}

	public void setQuitAction(Action quitAction) {
		Validate.notNull(quitAction, "No specified quit action");
		this.quitAction = quitAction;
	}

	protected abstract JComponent createContentPane();
  
  protected JComponent createOptionalPane() throws ResourcesException {
    return null;
  }

	protected abstract List createAdditionalActions();

	public void init() throws ResourcesException {
		LogFactory.getLog(getClass()).debug("init frame resources");

		String title = "FreeCast";
		try {
			title += " " + resources.getText("name");
		} catch (ResourcesException e) {
			LogFactory.getLog(getClass()).debug("No specified name", e);
		}
		setTitle(StringUtils.abbreviate(title.trim(), 20));

		Color background = resources.getColor("background");
		getContentPane().setBackground(background);

		JDialog logDialog = new LogDialog(this);
		logDialog.setLocationRelativeTo(this);

		if (quitAction == null) {
			quitAction = new QuitAction(resources);
		}
		aboutAction = new AboutAction(new AboutDialog(resources, this));
		logAction = new DisplayLogAction(logDialog);
		showHideMainAction = new ShowHideAction(resources, this, "Main dialog",
				"main");

		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(GAP, GAP, GAP, GAP);

		ToolbarPane toolbar = createToolBarPane();
		toolbar.setVisible(false); // toolbar is hidden by default

		showHideToolbarAction = new ShowHideAction(toolbar, "Toolbar") {
			private static final long serialVersionUID = 7508283130422886553L;

			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				pack();
			}
		};
		Icon toolbarIcon = resources.getIcon("toolbar");
		Actions.setSmallIcon(showHideToolbarAction, toolbarIcon);
		Icon logo = resources.getIcon("logo");
		Actions.setLargIcon(showHideToolbarAction, logo);

		GridBagConstraints logoConstraints = (GridBagConstraints) constraints
				.clone();
		IconButton logoButton = new IconButton(showHideToolbarAction);
		getContentPane().add(logoButton, logoConstraints);

		GridBagConstraints mainButtonConstraints = (GridBagConstraints) constraints
				.clone();
		mainButtonConstraints.gridwidth = GridBagConstraints.REMAINDER;
		IconButton mainButton = new IconButton(getButtonAction());
		getContentPane().add(mainButton, mainButtonConstraints);

		JComponent contentPane = createContentPane();
		if (contentPane != null) {
			GridBagConstraints contentConstraints = (GridBagConstraints) constraints
					.clone();
			contentConstraints.insets.left += GAP;
			contentConstraints.insets.right += GAP;
			contentConstraints.fill = GridBagConstraints.BOTH;
			contentConstraints.gridwidth = GridBagConstraints.REMAINDER;
			getContentPane().add(contentPane, contentConstraints);
		}

		GridBagConstraints toolbarConstraints = (GridBagConstraints) constraints
				.clone();
		toolbarConstraints.gridwidth = GridBagConstraints.REMAINDER;
		toolbarConstraints.fill = GridBagConstraints.HORIZONTAL;
		toolbarConstraints.weightx = 1.0;
		toolbarConstraints.insets.top = 0;
		getContentPane().add(toolbar, toolbarConstraints);

		PopupListener popupListener = new PopupListener(createPopupMenu());
		PopupListener.addMouseListener(getContentPane(), popupListener);

		pack();
		setResizable(false);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			
			private ActionEventFactory factory = new ActionEventFactory(BaseFrame.this);
			
			public void windowClosing(WindowEvent event) {
				ActionEvent actionEvent = factory.createActionEvent();
				quitAction.actionPerformed(actionEvent);
			}
		});

		try {
			installSystemTray();
		} catch (Throwable e) {
			LogFactory.getLog(getClass()).error(
					"Can't initialize the tray toolbarIcon", e);
			showHideMainAction.setEnabled(false); // disable hide action if
													// the tray menu isn't
													// available
		}
	}

	protected Action getButtonAction() {
		return NullAction.getInstance();
	}

	private ToolbarPane createToolBarPane() throws ResourcesException {
		ToolbarPane toolbar = new ToolbarPane(createOptionalPane());
		toolbar.add(aboutAction);
		toolbar.add(logAction);
		toolbar.setMessageAction(logAction);
		return toolbar;
	}

	private JPopupMenu createPopupMenu() throws ResourcesException {
		final Color background = resources.getColor("background");
		JPopupMenu popMenu = new JPopupMenu() {
			private static final long serialVersionUID = 8550805039345504434L;

			public JMenuItem add(Action a) {
				JMenuItem item = super.add(a);
				item.setOpaque(false);
				return item;
			}
		};
		popMenu.setBackground(background);

		popMenu.add(showHideMainAction);
		popMenu.add(showHideToolbarAction);
		popMenu.addSeparator();
		List additionalActions = createAdditionalActions();
		for (Iterator iterator = additionalActions.iterator(); iterator
				.hasNext();) {
			Action action = (Action) iterator.next();
			popMenu.add(action);
		}
		if (!additionalActions.isEmpty()) {
			popMenu.addSeparator();
		}
		popMenu.add(quitAction);

		return popMenu;
	}

	private void installSystemTray() throws ResourcesException {
		TrayIcon trayIcon = new TrayIcon(resources.getIcon("tray"));
		trayIcon.setCaption("FreeCast");
		trayIcon.setIconAutoSize(true);
		trayIcon.setToolTip(getTitle());
		trayIcon.setPopupMenu(createPopupMenu());

		SystemTray.getDefaultSystemTray().addTrayIcon(trayIcon);
	}

}
