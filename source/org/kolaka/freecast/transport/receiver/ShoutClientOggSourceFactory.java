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

package org.kolaka.freecast.transport.receiver;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.ogg.OggSource;
import org.kolaka.freecast.ogg.OggStreamSource;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class ShoutClientOggSourceFactory implements OggSourceFactory {

	private final URL url;

	private final HttpClient httpClient;

	public ShoutClientOggSourceFactory(URL url) {
		this.url = url;
		this.httpClient = new HttpClient();
	}

	public OggSource next() throws IOException {
		GetMethod httpRetrieve = new GetMethod(url.toExternalForm());
		int statusCode = httpClient.executeMethod(httpRetrieve);

		if (statusCode != HttpStatus.SC_OK) {
			throw new HttpException("Can't connect to " + url + " ("
					+ httpRetrieve.getStatusLine() + ")");
		}

		LogFactory.getLog(getClass()).info(
				"ShoutClient successfully connected to " + url);

		return new OggStreamSource(httpRetrieve.getResponseBodyAsStream());
	}

}
