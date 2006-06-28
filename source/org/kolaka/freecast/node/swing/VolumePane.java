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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.kolaka.freecast.player.Player;
import org.kolaka.freecast.player.PlayerSource;
import org.kolaka.freecast.player.VolumeControl;
import org.kolaka.freecast.player.VolumeControlable;
import org.kolaka.freecast.player.PlayerSource.Listener;
import org.kolaka.freecast.service.Service;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;

public class VolumePane extends JPanel {

  private static final long serialVersionUID = -6343034639160378478L;
  private VolumeControl volumeControl;

  public VolumePane(Resources resources, PlayerSource source) throws ResourcesException {
    setLayout(new GridBagLayout());
    setOpaque(false);

    final BoundedRangeModel sliderModel = new DefaultBoundedRangeModel(0, 1, 0, 100);
    final JSlider volumeSlider = new JSlider(sliderModel);
    volumeSlider.setEnabled(false);
    volumeSlider.setOpaque(false);
    volumeSlider.setUI(new CustomizedSliderUI(volumeSlider));
    volumeSlider.setPaintTicks(false);
    volumeSlider.setBackground(resources.getColor("background"));
    
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.NONE;
    constraints.weightx = 0;
    add(volumeSlider, constraints);
    
    final MouseWheelListener wheelListener = new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (!volumeSlider.isEnabled()) {
          return;
        }
        
        int volumeDelta = 10;
        if (e.getWheelRotation() > 0) {
          volumeDelta = -volumeDelta;
        }
        sliderModel.setValue(sliderModel.getValue() + volumeDelta);
      }
    };
    volumeSlider.addMouseWheelListener(wheelListener);
    
    final Player.Listener playerListener = new Player.Adapter() {
      public void serviceStopped(Service service) {
        volumeControl = null;
        sliderModel.setValue(0);
        volumeSlider.setEnabled(false);
      }
    };

    source.addListener(new Listener() {
      public void playerCreated(Player player) {
        if (player instanceof VolumeControlable) {
          VolumeControl control = ((VolumeControlable) player).getVolumeControl();
          if (control != null && control.isEnabled()) {
            volumeControl = control;
            
            sliderModel.setValue(volumeControl.getVolume());
            volumeSlider.setEnabled(true);
            
            player.add(playerListener);
          }
        }
      }
    });
    
    sliderModel.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (volumeControl == null) {
          return;
        }
        
        volumeControl.setVolume(sliderModel.getValue());
      }
    });
  }
  
}
