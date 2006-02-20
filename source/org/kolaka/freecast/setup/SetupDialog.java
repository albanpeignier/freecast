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

package org.kolaka.freecast.setup;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.lang.mutable.ObservableValue;
import org.kolaka.freecast.resource.URIParser;
import org.kolaka.freecast.swing.ActionEventFactory;
import org.kolaka.freecast.swing.BaseAction;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;
import org.kolaka.freecast.transport.receiver.PlaylistEncoderReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.PlaylistReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;

public class SetupDialog extends JDialog {

	class FinishAction extends BaseAction {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -5907250022544864467L;
		private final ObservableValue configuration;
		private final ObservableValue editConfiguration;

		private FinishAction(Resources resources, ObservableValue publicConfiguration, ObservableValue editConfiguration) throws ResourcesException {
			super("Finish");
			this.configuration = publicConfiguration;
			this.editConfiguration = editConfiguration;
			loadIcons(resources, "finish");
		}

		public void actionPerformed(ActionEvent e) {
			configuration.setValue(editConfiguration.getValue());
			SetupDialog.this.dispose();
		}
	}

	class CancelAction extends BaseAction {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 2779828569087146054L;
		private final ObservableValue configuration;

		private CancelAction(Resources resources, ObservableValue configuration) throws ResourcesException {
			super("Cancel");
			this.configuration = configuration;
			loadIcons(resources, "cancel");
		}

		public void actionPerformed(ActionEvent e) {
			int option = JOptionPane.showConfirmDialog(SetupDialog.this, "Do you really want to cancel setup ?","Cancel Setup", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				configuration.setValue(null);
				SetupDialog.this.dispose();
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4277834281458989376L;
	private final Action cancelAction;
	private final Action finishAction;
	
	private static final ReceiverConfiguration DEFAULT_RECEIVERCONFIGURATION = createDefaultReceiverConfiguration();

	private static ReceiverConfiguration createDefaultReceiverConfiguration() {
		PlaylistReceiverConfiguration configuration = new PlaylistReceiverConfiguration();
		configuration.setUri(URI.create("http://download.freecast.org/jws/default/audio.m3u"));
		configuration.setBandwidth(40);
		return configuration;
	}
	
	private final ObservableValue modifiedReceiverConfiguration, publicReceiverConfiguration;
	
	public ObservableValue getReceiverConfiguration() {
		return publicReceiverConfiguration;
	}
	
	public void setReceiverConfiguration(ReceiverConfiguration receiverConfiguration) {
		publicReceiverConfiguration.setValue(receiverConfiguration);
	}

	public SetupDialog(Resources resources, JFrame parent) throws ResourcesException {
		super(parent, "FreeCast Setup");
		
		publicReceiverConfiguration = new ObservableValue(DEFAULT_RECEIVERCONFIGURATION);
		modifiedReceiverConfiguration = new ObservableValue(publicReceiverConfiguration.getValue());
		publicReceiverConfiguration.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				modifiedReceiverConfiguration.setValue(arg);
			}
		});
		
		cancelAction = new CancelAction(resources, publicReceiverConfiguration);

		finishAction = new FinishAction(resources, publicReceiverConfiguration, modifiedReceiverConfiguration);
		finishAction.setEnabled(false);
		
		modifiedReceiverConfiguration.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				ReceiverConfiguration configuration = (ReceiverConfiguration) arg;
				if (configuration == null) {
					return;
				}
				
				try {
					configuration.validate();
					finishAction.setEnabled(true);
				} catch (ReceiverConfiguration.ValidateException e) {
					finishAction.setEnabled(false);
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			ActionEventFactory factory = new ActionEventFactory(SetupDialog.this);
			
			public void windowClosing(WindowEvent e) {
				ActionEvent actionEvent = factory.createActionEvent();
				cancelAction.actionPerformed(actionEvent);
			}
		});
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		init(resources);
		
		modifiedReceiverConfiguration.setValue(DEFAULT_RECEIVERCONFIGURATION);

		setLocationRelativeTo(parent);
		pack();
	}

	private void init(Resources resources) {
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(10,10,10,10);
		
		Component body = createBodyPanel(resources);
		GridBagConstraints bodyConstraints = (GridBagConstraints) constraints.clone(); 
		contentPane.add(body, bodyConstraints);
		
		Component buttons = createButtonPanel();
		GridBagConstraints buttonConstraints = (GridBagConstraints) constraints.clone();
		buttonConstraints.anchor = GridBagConstraints.EAST;
		
		contentPane.add(buttons, buttonConstraints);
	}

	private Component createBodyPanel(final Resources resources) {
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5,5,5,5);
		
		GridBagConstraints textConstraints = (GridBagConstraints) constraints.clone(); 
		textConstraints.anchor = GridBagConstraints.WEST;
		textConstraints.insets.bottom += 5;
		panel.add(new JLabel("Choose what kind of stream you want broadcast:"), textConstraints);
		
		GridBagConstraints radioButtonConstraints = (GridBagConstraints) constraints.clone();
		radioButtonConstraints.anchor = GridBagConstraints.WEST;
		
		Action defaultReceiverAction = new BaseAction("Default test stream") {
			
			protected void init() throws ResourcesException {
				loadIcons(resources, "receiver.default");
			}
			
			public void actionPerformed(ActionEvent e) {
				modifiedReceiverConfiguration.setValue(DEFAULT_RECEIVERCONFIGURATION);
			}
		};
		final JRadioButton defaultReceiver = new JRadioButton(defaultReceiverAction);
		panel.add(defaultReceiver, radioButtonConstraints);
		modifiedReceiverConfiguration.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				boolean enabled = DEFAULT_RECEIVERCONFIGURATION.equals(arg);
				defaultReceiver.setSelected(enabled);
			}
		});
		
		JFormattedTextField.AbstractFormatter uriFormatter = new JFormattedTextField.AbstractFormatter() {
			private URIParser parser = new URIParser();
			public Object stringToValue(String text) throws ParseException {
				try {
					return parser.parse(text);
				} catch (URISyntaxException e) {
					throw new ParseException("Can't parse the specified URI", 0);
				}
			}
			public String valueToString(Object value) throws ParseException {
				if (value == null) {
					return "";
				}
				
				return value.toString();
			} 
		};
		
		final JFormattedTextField playlistField = new JFormattedTextField(uriFormatter);
		playlistField.setColumns(30);
		playlistField.setEnabled(false);
		playlistField.setEditable(false);
		
		playlistField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				textChanged();
			}
			public void insertUpdate(DocumentEvent e) {
				textChanged();
			}
			public void removeUpdate(DocumentEvent e) {
				textChanged();
			}
			private void textChanged() {
				URI uri = null;

				try {
					playlistField.commitEdit();
					uri = (URI) playlistField.getValue();
				} catch (ParseException e) {
					LogFactory.getLog(getClass()).debug("can't parse uri",e);
				}
				
				PlaylistEncoderReceiverConfiguration configuration =
					(PlaylistEncoderReceiverConfiguration) modifiedReceiverConfiguration.getValue();
				configuration.setUri(uri);
				modifiedReceiverConfiguration.setValue(configuration);
			}
		});

			final FileFilter filter = new FileFilter() {
					public boolean accept(File f) {
						String filename = f.getName().toLowerCase();
					return filename.endsWith(".m3u");
					}
					public String getDescription() {
						return "M3U playlist file";
					}
			};
		
		Action chooseFileAction = new BaseAction("Choose file") {

			public void actionPerformed(ActionEvent e) {
				PlaylistEncoderReceiverConfiguration configuration =
					(PlaylistEncoderReceiverConfiguration) modifiedReceiverConfiguration.getValue();
				URI uri = configuration.getUri();
				
				JFileChooser chooser = new JFileChooser();
			    chooser.setFileFilter(filter);
			    
			    
			    if (uri != null) {
			    	File selectedFile = new File(uri.getPath());
			    	chooser.setCurrentDirectory(selectedFile.getParentFile());
			    }
			    
			    
			    int returnVal = chooser.showOpenDialog(SetupDialog.this);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	File selectedFile = chooser.getSelectedFile();
			    		configuration.setUri(selectedFile.toURI());
			    		modifiedReceiverConfiguration.setValue(configuration);
			    }
			}
		};
		
		final PlaylistEncoderReceiverConfiguration playlistEncoderReceiverConfiguration 
			= new PlaylistEncoderReceiverConfiguration();

		Action playlistReceiverAction = new BaseAction("My own playlist") {
			
			protected void init() throws ResourcesException {
				loadIcons(resources, "receiver.playlist");
			}
			
			public void actionPerformed(ActionEvent e) {
				modifiedReceiverConfiguration.setValue(playlistEncoderReceiverConfiguration);
			}
		};
		
		final JRadioButton playlistReceiver = new JRadioButton(playlistReceiverAction);
		modifiedReceiverConfiguration.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				boolean enabled = arg instanceof PlaylistEncoderReceiverConfiguration;
				playlistReceiver.setSelected(enabled);
			}
		});
		
		panel.add(playlistReceiver, radioButtonConstraints);
		
		GridBagConstraints playlistFieldConstraints = (GridBagConstraints) constraints.clone();
		playlistFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		playlistFieldConstraints.weightx = 1;
		playlistFieldConstraints.gridwidth = 1;
		panel.add(playlistField, playlistFieldConstraints);
		
		GridBagConstraints chooseFileConstraints = (GridBagConstraints) constraints.clone();
		final JButton chooseFileButton = new JButton(chooseFileAction);
		panel.add(chooseFileButton, chooseFileConstraints);
		
		modifiedReceiverConfiguration.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				boolean enabled = (arg instanceof PlaylistEncoderReceiverConfiguration);
				playlistField.setEnabled(enabled);
				chooseFileButton.setEnabled(enabled);
				
				if (enabled) {
					PlaylistEncoderReceiverConfiguration configuration = (PlaylistEncoderReceiverConfiguration) arg;
					
					URI uri = configuration.getUri();
					if (uri != null && !uri.equals(playlistField.getValue())) {
						playlistField.setValue(uri);
					}
				}
			}
		});
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(defaultReceiver);
		buttonGroup.add(playlistReceiver);
		
		return panel;
	}
	
	private Component createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JButton(cancelAction));
		panel.add(new JButton(finishAction));
		return panel;
	}

}
