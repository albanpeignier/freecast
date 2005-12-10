/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004-2005 Alban Peignier
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
package org.kolaka.freecast.manager.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class HomeServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1087374272834762127L;

	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		httpServletResponse.setContentType("text/html");
		PrintWriter out = httpServletResponse.getWriter();

		Transformer transformer;

		try {
			InputStream input = getClass().getResourceAsStream(
					"resources/home.xsl");
			TransformerFactory factory = TransformerFactory.newInstance();
			transformer = factory.newTransformer(new StreamSource(input));
		} catch (TransformerConfigurationException e) {
			throw new ServletException("Can't configure the XSL transformer", e);
		}

		String host = httpServletRequest.getServerName();
		transformer.setParameter("host", host);

		String servername = getInitParameter("servername");
		transformer.setParameter("descriptor", servername + ":"
				+ httpServletRequest.getServerPort() + "/descriptor.xml");

		try {
			transformer.transform(new StreamSource(new StringReader(
					"<empty></empty>")), new StreamResult(out));
		} catch (TransformerException e) {
			throw new ServletException("Can't create the descriptor content", e);
		}

		out.close();
	}

}
