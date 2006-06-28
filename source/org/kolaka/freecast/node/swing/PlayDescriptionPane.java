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

package org.kolaka.freecast.node.swing;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.jdic.desktop.Desktop;
import org.kolaka.freecast.player.PlayDescription;
import org.kolaka.freecast.player.PlayDescriptionHandler;
import org.kolaka.freecast.player.PlayDescriptionProvider;
import org.kolaka.freecast.player.Player;
import org.kolaka.freecast.player.PlayerSource;
import org.kolaka.freecast.player.PlayerSource.Listener;
import org.kolaka.freecast.swing.ActionEventFactory;
import org.kolaka.freecast.swing.AsyncAction;

public class PlayDescriptionPane extends JPanel {

  private static final long serialVersionUID = -3816636595541185609L;

  private PlayDescription playDescription;
  
  public PlayDescriptionPane(PlayerSource playerSource) {
    setOpaque(false);
    
    final JLabel label = new JLabel("No description available");

    final Action visitAction = new AsyncAction(new AbstractAction() {

      private static final long serialVersionUID = 0L;

      public void actionPerformed(ActionEvent event) {
        if (playDescription == null) {
          return;
        }
        
        URL url = playDescription.getURL();
        if (url == null) {
          return;
        }

        LogFactory.getLog(getClass()).debug("visit " + url);

        try {
          Desktop.browse(url);
        } catch (Exception e) {
          LogFactory.getLog(getClass()).error(
              "can't start a browser to visit " + url, e);
        }
      }
    });
    
    label.addMouseListener(new MouseAdapter() {
      private ActionEventFactory factory = new ActionEventFactory(PlayDescriptionPane.this);
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          ActionEvent actionEvent = factory.createActionEvent();
          visitAction.actionPerformed(actionEvent);
        }
      }

    });

    final PlayDescriptionHandler descriptionHandler = new PlayDescriptionHandler() {
          public void descriptionChanged(PlayDescription description) {
            playDescription = description;
            
            label.setText(StringUtils.abbreviate(description.getShortDescription(), 40));
            label.setToolTipText(description.getLongDescription());
          }
        };

    playerSource.addListener(new Listener() {
      public void playerCreated(Player player) {
        if (player instanceof PlayDescriptionProvider) {
          ((PlayDescriptionProvider) player).setPlayDescriptionHandler(descriptionHandler);
        }
      }
    });

    add(label);
  }
  
  
  
}
