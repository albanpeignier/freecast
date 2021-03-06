/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2006 Alban Peignier
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

package org.kolaka.freecast.manager.gui;

import java.awt.event.ActionEvent;
import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.jdesktop.jdic.desktop.Desktop;
import org.kolaka.freecast.swing.BaseAction;
import org.kolaka.freecast.swing.Resources;
import org.kolaka.freecast.swing.ResourcesException;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class BrowseHomepageAction extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1014721912985395264L;

	private final URL listenPage;
  private final boolean localListenPage;

	public BrowseHomepageAction(Resources resources,
			URL listenPage, boolean localListenPage) throws ResourcesException {
		super("Browse network homepage");
		this.listenPage = listenPage;
    this.localListenPage = localListenPage;
		loadIcons(resources, "visit.browse");
	}

	public void actionPerformed(ActionEvent event) {
    URL url = listenPage;
		try {
      if (localListenPage) {
        url = new URL(listenPage.getPath(), "localhost", listenPage.getPort(), listenPage.getPath());
      }
          
			LogFactory.getLog(getClass()).debug("browse " + url);
			Desktop.browse(url);
		} catch (Exception e) {
			LogFactory.getLog(getClass()).error(
					"can't start a browser to visit " + url, e);
		}
	}

}
