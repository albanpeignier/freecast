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
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ShoutClientReceiverConfiguration;
import org.pietschy.wizard.InvalidStateException;

public class ReceiverShoutClientStep extends ReceiverWizardStep {

  private static final long serialVersionUID = -7503124420884640976L;

  protected JComponent createView() {
    JPanel panel = new JPanel(new GridBagLayout());

    GridBagConstraints labelConstraints = createConstraints();
    labelConstraints.anchor = GridBagConstraints.WEST;
    labelConstraints.gridwidth = GridBagConstraints.REMAINDER;
    panel.add(new JLabel("<html><b>Stream URL</b> :"), labelConstraints);
    
    GridBagConstraints fieldConstraints = createConstraints();
    
    urlField = new JFormattedTextField(new URLFormatter());
    urlField.setColumns(25);
    
    new DocumentValidator(urlField) {
      protected void documentValidated(boolean validated, Object value) {
        ReceiverShoutClientStep.this.setComplete(validated);
      }
    };

    panel.add(urlField, fieldConstraints);
    return panel;
  }

  private JFormattedTextField urlField;
  
  protected void prepare(ReceiverConfiguration configuration) {
    prepare((ShoutClientReceiverConfiguration) configuration);
  }
  
  protected void prepare(ShoutClientReceiverConfiguration configuration) {
    urlField.setValue(configuration.getUrl());
  }

  protected void applyState(ReceiverConfiguration configuration) throws InvalidStateException {
    applyState((ShoutClientReceiverConfiguration) configuration);
  }
  
  private void applyState(ShoutClientReceiverConfiguration configuration) throws InvalidStateException {
    URL url = (URL) urlField.getValue();
    if (url == null) {
      throw new IllegalStateException("No specified URL");
    }
    configuration.setUrl(url);
  }

}
