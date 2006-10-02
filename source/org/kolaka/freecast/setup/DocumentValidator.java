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

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.logging.LogFactory;

public abstract class DocumentValidator {
  
  public DocumentValidator(final JFormattedTextField field) {
    field.getDocument().addDocumentListener(new DocumentListener() {
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
        Object value = null;
        try {
          field.commitEdit();
          value = field.getValue();
        } catch (ParseException e) {
          LogFactory.getLog(getClass()).debug("can't parse value", e);
          return;
        }

        documentValidated(value != null, value);
      }

    });
  }

  protected abstract void documentValidated(boolean validated, Object value);

}
