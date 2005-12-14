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

package org.kolaka.freecast.swing;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.apache.commons.lang.exception.ExceptionUtils;

public class ErrorPane {

	private Component parent;
	
	public ErrorPane() {
		
	}
	
	public ErrorPane(Component parent) {
		this.parent = parent;
	}
	
	public void show(String failedAction, Throwable cause) {
		String stackTrace = ExceptionUtils.getFullStackTrace(cause).replaceAll(
				"\n", "<br>");

		String message = "<html><p>FreeCast can't start because of the following error:</p>&nbsp;"
				+ "<p><b>"
				+ cause.getMessage()
				+ "</b><br>"
				+ stackTrace
				+ "</p>&nbsp;"
				+ "<p>If needed, visit http://www.freecast.org/support</p>&nbsp;";
		JOptionPane.showMessageDialog(parent, message,
				failedAction, JOptionPane.ERROR_MESSAGE);		
	}

}
