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

package org.kolaka.freecast.player;

import java.util.Arrays;

import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.logging.LogFactory;

public class LineVolumeControl implements VolumeControl {

  private FloatControl control;
  
  private static final Control.Type[] CONTROL_TYPES = new Control.Type[] {
    FloatControl.Type.MASTER_GAIN, FloatControl.Type.VOLUME
  };
  
  public LineVolumeControl(SourceDataLine line) {
    control = selectControl(line);
    LogFactory.getLog(getClass()).debug("create LineVolumeControl with " + control);
  }

  private FloatControl selectControl(SourceDataLine line) {
    LogFactory.getLog(getClass()).debug("looking for volume control into " + Arrays.asList(line.getControls()));
    
    for (int i=0; i < CONTROL_TYPES.length; i++) {
      try {
        return (FloatControl) line.getControl(CONTROL_TYPES[i]);
      } catch (IllegalArgumentException e) {
        LogFactory.getLog(getClass()).trace("No control associated to " + CONTROL_TYPES[i]);
      }    
    }
    return null;
  }

  // volume = (value - min) / (max - min) * 100
  public int getVolume() {
    if (!isEnabled()) {
      throw new IllegalStateException("Disabled volume control");
    }
    
    float value = control.getValue();
    int volume = (int) ((value - control.getMinimum()) / (control.getMaximum() - control.getMinimum()) * 100);
    LogFactory.getLog(getClass()).trace("value: " + value + " volume: " + volume);
    return volume; 
  }

  // value = min + (volume * (max - min) / 100)  
  public void setVolume(int volume) {
    if (!isEnabled()) {
      throw new IllegalStateException("Disabled volume control");
    }

    float value = control.getMinimum() + ((float) volume / 100 * (control.getMaximum() - control.getMinimum()));
    LogFactory.getLog(getClass()).trace("volume: " + volume + " value: " + value);
    control.setValue(value);
  }
  
  public boolean isEnabled() {
    return control != null;
  }

}
