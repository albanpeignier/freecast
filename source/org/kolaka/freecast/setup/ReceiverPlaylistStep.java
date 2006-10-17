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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.kolaka.freecast.swing.BaseAction;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.transport.receiver.PlaylistEncoderReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;
import org.pietschy.wizard.InvalidStateException;

public class ReceiverPlaylistStep extends ReceiverConfigurationWizardStep {

  public ReceiverPlaylistStep(Resources resources) {
    super(resources);
  }

  private static final long serialVersionUID = -7503124420884640976L;
  
  protected JComponent createView() {
    JPanel panel = new JPanel(new GridBagLayout());
    
    JLabel label = new JLabel("<html><b>Playlist</b> :");
    GridBagConstraints labelConstraints = createConstraints();
    labelConstraints.anchor = GridBagConstraints.WEST;
    labelConstraints.gridwidth = GridBagConstraints.REMAINDER;
    panel.add(label, labelConstraints);
    
    playlistField = new JFormattedTextField(new URIFormatter());
    playlistField.setColumns(25);

    GridBagConstraints playlistFieldConstraints = createConstraints();
    panel.add(playlistField, playlistFieldConstraints);

    new DocumentValidator(playlistField) {
      protected void documentValidated(boolean validated, Object value) {
        ReceiverPlaylistStep.this.setComplete(validated);
      }
    };

    Action chooseFileAction = new BaseAction("Choose file") {

      private static final long serialVersionUID = -1643278453196884836L;

      public void actionPerformed(ActionEvent e) {
        URI uri = (URI) playlistField.getValue();

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileExtensionFilter("m3u", "M3U playlist file"));

        if (uri != null) {
          File selectedFile = new File(uri.getPath());
          chooser.setCurrentDirectory(selectedFile.getParentFile());
        }

        int returnVal = chooser.showOpenDialog(ReceiverPlaylistStep.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File selectedFile = chooser.getSelectedFile();
          playlistField.setValue(selectedFile.toURI());
        }
      }
    };

    GridBagConstraints chooseFileConstraints = createConstraints();
    final JButton chooseFileButton = new JButton(chooseFileAction);
    panel.add(chooseFileButton, chooseFileConstraints);
    
    return panel;
  }

  private JFormattedTextField playlistField;

  protected void prepare(ReceiverConfiguration configuration) {
    prepare((PlaylistEncoderReceiverConfiguration) configuration);
  }
  
  private void prepare(PlaylistEncoderReceiverConfiguration configuration) {
    URI uri = configuration.getUri();
    if (uri != null && !uri.equals(playlistField.getValue())) {
      playlistField.setValue(uri);
    }
  }
  
  protected void applyState(ReceiverConfiguration configuration) throws InvalidStateException {
    applyState((PlaylistEncoderReceiverConfiguration) configuration);
  }

  private void applyState(PlaylistEncoderReceiverConfiguration configuration) throws InvalidStateException {
    URI uri = (URI) playlistField.getValue();
    if (uri == null) {
      throw new InvalidStateException("No specified playlist");
    }
    configuration.setUri(uri);
  }

}
