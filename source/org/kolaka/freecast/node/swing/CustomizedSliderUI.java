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

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class CustomizedSliderUI extends BasicSliderUI {

  public CustomizedSliderUI(JSlider slider) {
    super(slider);
  }

  /*
   * public void paintThumb(Graphics g) {
   *  }
   */

  protected Dimension getThumbSize() {
    Dimension size = new Dimension();

    int width = 10;
    int height = 5;
    
    if (slider.getOrientation() == JSlider.VERTICAL) {
      size.width = width;
      size.height = height;
    } else {
      size.width = height;
      size.height = width;
    }

    return size;
  }
  
  public void paintFocus(Graphics g) {

  }
  
  public Dimension getPreferredHorizontalSize() {
    Dimension dimension = super.getPreferredHorizontalSize();
    dimension.width = 100;
    return dimension;
  }

}
