package org.kolaka.freecast.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URLTextFile extends TextFile {

	private final URL url;

	public URLTextFile(File file) throws MalformedURLException {
		this(file.toURL());
	}

	public URLTextFile(URL url) {
		this.url = url;
	}

	public URLTextFile(URI uri) throws MalformedURLException {
		this(uri.toURL());
	}

	public void load() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(url
				.openStream()));
		load(reader);
	}

	protected void checkLoading() throws IOException {
		if (!isLoaded()) {
			load();
		}
	}

}