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

import org.kolaka.freecast.cortado.CortadoPanel;
import org.kolaka.freecast.service.ControlException;

import javax.swing.*;

/**
 * 
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class VideoPlayerSource extends BasePlayerSource implements
        InteractivePlayerSource, GraphicalPlayerSource {

    private CortadoPanel panel;
    private boolean autoPlayerCreation;

    public VideoPlayerSource(boolean autoPlayerCreation) {
        this.autoPlayerCreation = autoPlayerCreation;
        panel = new CortadoPanel();
    }

	public void setAudio(boolean audio) {
		panel.setAudio(audio);
	}

	public void setFrameRate(int frameRate) {
		panel.setFrameRate(frameRate);
	}

    public void start() throws ControlException {
        super.start();

        if (autoPlayerCreation) {
            createPlayer();
        }
    }

    public void createPlayer() {
        VideoPlayer player = new VideoPlayer(panel);
        processPlayerCreated(player);
    }

    public JComponent getJComponent() {
        return panel;
    }
    

}
