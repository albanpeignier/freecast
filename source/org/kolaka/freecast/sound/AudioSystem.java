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

package org.kolaka.freecast.sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.LogFactory;

import sun.misc.Service;
import sun.misc.ServiceConfigurationError;

public class AudioSystem {

	public static AudioInputStream getAudioInputStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		Iterator providers = getProviders(AudioFileReader.class);

		while (providers.hasNext()) {
			AudioFileReader reader = (AudioFileReader) providers.next();
			LogFactory.getLog(AudioSystem.class).debug(
					"try to open stream with " + reader);
			try {
				AudioInputStream audioStream = reader
						.getAudioInputStream(stream);
				LogFactory.getLog(AudioSystem.class).debug(
						"stream opened with " + reader);
				return audioStream;
			} catch (UnsupportedAudioFileException e) {

			}
		}

		throw new UnsupportedAudioFileException(
				"could not get audio input stream from input stream");
	}

	public static AudioInputStream getAudioInputStream(
			AudioFormat targetFormat, AudioInputStream sourceStream) {
		AudioFormat sourceFormat = sourceStream.getFormat();
		if (sourceFormat.matches(targetFormat)) {
			return sourceStream;
		}

		LogFactory.getLog(AudioSystem.class).debug(
				"try to find a codec to transform " + sourceFormat + " into "
						+ targetFormat);

		Iterator providers = getProviders(FormatConversionProvider.class);
		while (providers.hasNext()) {
			FormatConversionProvider codec = (FormatConversionProvider) providers
					.next();
			LogFactory.getLog(AudioSystem.class).debug(
					"test next codec " + codec);
			if (codec.isConversionSupported(targetFormat, sourceFormat)) {
				LogFactory.getLog(AudioSystem.class).debug(
						"find compatible codec to transform " + sourceFormat
								+ " into " + targetFormat + ": " + codec);
				return codec.getAudioInputStream(targetFormat, sourceStream);
			}
		}

		throw new IllegalArgumentException("Unsupported conversion: "
				+ targetFormat + " from " + sourceFormat);
	}

	/**
	 * Prefer full-Java implementations to native ones
	 */
	private static final List FAVORITE_SELECTORS = Arrays
			.asList(new ProviderSelector[] {
					new PackageProviderSelector("javazoom"),
					new PackageProviderSelector("org.tritonus") });

	/**
	 * @param providerClass
	 *            TODO
	 * @return
	 * @throws ServiceConfigurationError
	 */
	private static Iterator getProviders(Class providerClass)
			throws ServiceConfigurationError {
		List availables = new LinkedList();

		for (Iterator iter = Service.providers(providerClass); iter.hasNext();) {
			try {
				availables.add(iter.next());
			} catch (Throwable t) {
				LogFactory.getLog(AudioSystem.class).error(
						"can't load one of the provider for "
								+ providerClass.getName(), t);
				continue;
			}
		}

		final List favorites = selectFavorites(availables);

		List providers = new LinkedList();
		providers.addAll(favorites);

		Predicate otherPredicate = new Predicate() {
			public boolean evaluate(Object input) {
				return !favorites.contains(input);
			}
		};
		providers.addAll(CollectionUtils.select(availables, otherPredicate));

		return providers.iterator();
	}

	private static List selectFavorites(List providers) {
		final List favorites = new LinkedList();
		for (Iterator iter = FAVORITE_SELECTORS.iterator(); iter.hasNext();) {
			final ProviderSelector selector = (ProviderSelector) iter.next();
			Predicate selectorPredicate = new Predicate() {
				public boolean evaluate(Object input) {
					return selector.select(input) && !favorites.contains(input);
				}
			};
			favorites.addAll(CollectionUtils.select(providers,
					selectorPredicate));
		}
		return favorites;
	}

	public static interface ProviderSelector {

		public boolean select(Object provider);

	}

	public static class PackageProviderSelector implements ProviderSelector {

		private final String prefix;

		/**
		 * @param prefix
		 */
		public PackageProviderSelector(String prefix) {
			this.prefix = prefix;
		}

		public boolean select(Object provider) {
			return provider.getClass().getName().startsWith(prefix);
		}

	}

}
