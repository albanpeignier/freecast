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
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.swing.Resources;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

public class ReceiverTypeStep extends ReceiverWizardStep {

  private static final long serialVersionUID = 3952637776668476330L;
  
  private static String[] TYPES = new String[] {
    ReceiverWizardModel.TYPE_DEFAULT, ReceiverWizardModel.TYPE_PLAYLIST,
    ReceiverWizardModel.TYPE_SHOUTCLIENT, ReceiverWizardModel.TYPE_SHOUTSERVER
  };
  
  private String type;
  private Map buttons = new TreeMap();

  public ReceiverTypeStep(Resources resources) {
    super(resources);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(10, 10, 10, 10);
    constraints.anchor = GridBagConstraints.WEST;

    ButtonGroup group = new ButtonGroup();
    
    for (int i=0; i < TYPES.length; i++) {
      final String type = TYPES[i];

      final JRadioButton button = new JRadioButton(getBundle().getString(type));
      buttons.put(type, button);
      button.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (button.isSelected()) {
            ReceiverTypeStep.this.setComplete(true);
            ReceiverTypeStep.this.type = type;
          }
        }
      });
      
      group.add(button);
      add(button, constraints);
    }
  }
  
  public void prepare() {
    JRadioButton button = (JRadioButton) buttons.get(model.getType());
    if (button == null) {
      LogFactory.getLog(getClass()).warn("unknown ReceiverConfiguration type: " + model.getType());
    }
    button.setSelected(true);
    button.requestFocus();
  }
  
  public void applyState() throws InvalidStateException {
    model.createConfiguration(type);
  }

  private ReceiverWizardModel model;

  public void init(WizardModel model) {
    super.init(model);
    this.model = (ReceiverWizardModel) model;
  }

}