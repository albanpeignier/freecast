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
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.kolaka.freecast.swing.ResourceBundles;
import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

public abstract class ReceiverWizardStep extends PanelWizardStep {

  private final ResourceBundle bundle = ResourceBundles.getBundle(getClass());
  
  protected ReceiverWizardModel model;

  protected ReceiverWizardStep() {
    super("Setup Broadcast content", "");
    setSummary(bundle.getString("summary"));
    setLayout(new GridBagLayout());
    
    GridBagConstraints textConstraints = createConstraints();
    textConstraints.gridwidth = GridBagConstraints.REMAINDER;
    textConstraints.insets = new Insets(15, 0, 15, 0);
    add(new JLabel(bundle.getString("explanation")), textConstraints);
    
    GridBagConstraints viewConstraints = createConstraints();
    viewConstraints.gridwidth = GridBagConstraints.REMAINDER;
    
    JComponent view = createView();
    add(view, textConstraints);
  }
  
  protected ResourceBundle getBundle() {
    return bundle;
  }

  protected abstract JComponent createView();

  /**
   * @return
   */
  protected GridBagConstraints createConstraints() {
    return new GridBagConstraints();
  }

  public void init(WizardModel model) {
    this.model = (ReceiverWizardModel) model;
  }
  
  public void prepare() {
    ReceiverConfiguration configuration = model.getConfiguration();
    prepare(configuration);
  }
  
  protected abstract void prepare(ReceiverConfiguration configuration);
  
  public void applyState() throws InvalidStateException {
    applyState(model.getConfiguration());
  }
  
  protected abstract void applyState(ReceiverConfiguration configuration) throws InvalidStateException;
  
}
