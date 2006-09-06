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

package org.kolaka.freecast;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kolaka.freecast.config.ConfigurationLoader;
import org.kolaka.freecast.config.DefaultConfigurationLoader;
import org.kolaka.freecast.resource.ClassLoaderResourceLocator;
import org.kolaka.freecast.resource.CompositeResourceLocator;
import org.kolaka.freecast.resource.FileResourceLocator;
import org.kolaka.freecast.resource.HttpResourceLocator;
import org.kolaka.freecast.resource.ResourceLocator;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public abstract class Application {

	private final String name;

	private final ResourceLocator resourceLocator;

	private Configuration configuration;

	protected Application(String name) {
		this.name = name;
		this.resourceLocator = createResourceLocator();
	}

	private ResourceLocator createResourceLocator() {
		CompositeResourceLocator locator = new CompositeResourceLocator();

		locator
				.add(new ClassLoaderResourceLocator(getClass().getClassLoader()));

		File cacheDirectory = HttpResourceLocator.PersistentFileCache.getDefaultCacheDirectory();
		cacheDirectory.mkdirs();

		locator.add(new HttpResourceLocator(
				new HttpResourceLocator.PersistentFileCache(cacheDirectory)));
		locator.add(new FileResourceLocator());
		locator.add(new FileResourceLocator(new File(SystemUtils.USER_HOME)));

		return locator;
	}

	protected ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public String getName() {
		return name;
	}

	public boolean init(String args[]) throws Exception {
		if (!LogFactory.getLog(getClass()).isDebugEnabled()) {
			StandardToStringStyle toStringStyle = new StandardToStringStyle();
			toStringStyle.setUseShortClassName(true);
			toStringStyle.setUseIdentityHashCode(false);
			ToStringBuilder.setDefaultStyle(toStringStyle);
		}

		Options options = new Options();

		Option help = new Option("help", "print this message");
		options.addOption(help);

		Option configOption = new Option("config", true,
				"specifies the uri of the config file");
		configOption.setArgName("uri");
		options.addOption(configOption);

		Option dryrunOption = new Option("dryrun", false,
				"only load and test the configuration");
		options.addOption(dryrunOption);

    Option debugOption = new Option("debug", false,
    "set log level to debug");
    options.addOption(debugOption);

		Option propertyOption = new Option("D", true,
				"specifies a configuration property");
		propertyOption.setArgName("value=property");
		propertyOption.setValueSeparator('=');
		propertyOption.setArgs(2);
		options.addOption(propertyOption);

		CommandLineParser parser = new GnuParser();
		CommandLine line;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			help("Parsing failed.  " + e.getMessage(), options);
			return false;
		}

		if (line.hasOption(help.getOpt())) {
			help("", options);
			return false;
		}

    boolean debug = line.hasOption(debugOption.getOpt());
    if (debug) {
      Logger.getLogger("org.kolaka.freecast").setLevel(Level.DEBUG);
      LogFactory.getLog(getClass()).debug("debug level set by command line");
    }

		boolean dryrun = line.hasOption(dryrunOption.getOpt());

		ConfigurationLoader loader = createConfigurationLoader();
		loader.setResourceLocator(getResourceLocator());

		if (line.hasOption(configOption.getOpt())) {
			URI userURI = new URI(line.getOptionValue(configOption.getOpt()));
			loader.setUserURI(userURI);
		}
		if (line.hasOption(propertyOption.getOpt())) {
			String values[] = line.getOptionValues(propertyOption.getOpt());
			for (int i = 0; i < values.length; i += 2) {
				loader.addUserProperty(values[i], values[i + 1]);
			}
		}

		loader.load();

		this.configuration = loader.getRootConfiguration();
		postInit(configuration);

		return !dryrun;
	}

	protected final ConfigurationLoader createConfigurationLoader() {
		return new DefaultConfigurationLoader(getName());
	}

	protected abstract void postInit(Configuration configuration)
			throws Exception;

	public final void run(String args[]) {
		try {
			LogFactory.getLog(getClass()).info(
					"version " + Version.getInstance().getName());
      LogFactory.getLog(getClass()).debug("runtime version " + SystemUtils.JAVA_RUNTIME_VERSION);
			LogFactory.getLog(getClass()).debug("init");
			if (init(args)) {
				LogFactory.getLog(getClass()).debug("start");
				run();
			}
		} catch (Throwable e) {
			displayFatalError(e);
			System.exit(1);
		}

		LogFactory.getLog(getClass()).debug("exit");
		System.exit(0);
	}

	protected void displayFatalError(Throwable cause) {
		LogFactory.getLog(Application.class).fatal("Error in the main thread",
				cause);
	}

	protected void displayHelper(String message, String usage) {
		if (!StringUtils.isEmpty(message)) {
			System.out.println(message);
		}
		System.out.println(usage);
	}

	protected abstract void run() throws Exception;

	/**
	 * @param options
	 */
	protected void help(String message, Options options) {
		HelpFormatter formatter = new HelpFormatter();

		String applicationName = System.getProperty("app.name", "freecast-"
				+ name);

		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);

		formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH,
				applicationName, "", options, HelpFormatter.DEFAULT_LEFT_PAD,
				HelpFormatter.DEFAULT_DESC_PAD, "");

		writer.close();

		displayHelper(message, stringWriter.getBuffer().toString());
	}

}