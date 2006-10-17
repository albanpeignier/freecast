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
import java.net.InetSocketAddress;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ShoutServerReceiverConfiguration;
import org.pietschy.wizard.InvalidStateException;

public class ReceiverShoutServerStep extends ReceiverConfigurationWizardStep {

  protected ReceiverShoutServerStep(Resources resources) {
    super(resources);
  }

  private static final long serialVersionUID = -7503124420884640976L;

  protected JComponent createView() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints labelConstraints = createConstraints();
    panel.add(new JLabel("<html><b>Port</b> :"), labelConstraints);
    
    GridBagConstraints portFieldConstraints = createConstraints();
    
    portField = new JFormattedTextField(new NumberFormatter(new DecimalFormat(
        "#0")));
    portField.setColumns(5);
    
    new DocumentValidator(portField) {
      protected void documentValidated(boolean validated, Object value) {
        ReceiverShoutServerStep.this.setComplete(validated);
      }
    };

    panel.add(portField, portFieldConstraints);
    return panel;
  }

  private JFormattedTextField portField;
  
  protected void prepare(ReceiverConfiguration configuration) {
    prepare((ShoutServerReceiverConfiguration) configuration);
  }
  
  protected void prepare(ShoutServerReceiverConfiguration configuration) {
    Integer port = new Integer(configuration.getListenAddress().getPort());
    portField.setValue(port);
  }

  protected void applyState(ReceiverConfiguration configuration) throws InvalidStateException {
    applyState((ShoutServerReceiverConfiguration) configuration);
  }
  
  private void applyState(ShoutServerReceiverConfiguration configuration) throws InvalidStateException {
    Number port = (Number) portField.getValue();
    if (port == null) {
      throw new IllegalStateException("No specified port");
    }
    InetSocketAddress listenAddress = new InetSocketAddress(port.intValue());
    configuration.setListenAddress(listenAddress);
  }

}
