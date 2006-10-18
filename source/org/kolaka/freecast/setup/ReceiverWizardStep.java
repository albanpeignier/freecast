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
import java.util.ResourceBundle;

import org.kolaka.freecast.swing.ResourceBundles;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

public abstract class ReceiverWizardStep extends PanelWizardStep {

  private final ResourceBundle bundle = ResourceBundles.getBundle(getClass());

  protected ReceiverWizardModel model;

  protected ResourceBundle getBundle() {
    return bundle;
  }

  public ReceiverWizardStep(Resources resources) {
    super("Setup Broadcast content","");
    setIcon(resources.getIcon("main.larg"));
    setSummary(bundle.getString("summary"));
    setLayout(new GridBagLayout());
  }  

  public void init(WizardModel model) {
    this.model = (ReceiverWizardModel) model;
  }
  
  /**
   * @return
   */
  protected GridBagConstraints createConstraints() {
    return new GridBagConstraints();
  }

}
