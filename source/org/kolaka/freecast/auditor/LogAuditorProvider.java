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
package org.kolaka.freecast.auditor;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class LogAuditorProvider extends ProxyAuditorProvider {

	public LogAuditorProvider(Class auditorInterface) {
		super(auditorInterface);
	}

	protected Auditor createAuditor(Class auditorInterface) {
		ResourceBundle logFormats;
		try {
			logFormats = loadResourceBundle(auditorInterface);
		} catch (MissingResourceException e) {
			LogFactory.getLog(getClass()).trace("No log message formats found for " + auditorInterface);
			return new NullAuditorProvider(auditorInterface).getAuditor();
		}
		Log log = getLog(auditorInterface);
		InvocationHandler handler = new LogInvocationHandler(logFormats, log);
        return createAuditor(auditorInterface,handler);
	}

	private Log getLog(Class auditorInterface) {
		Log log = LogFactory.getLog(auditorInterface);
		return log;
	}

	private ResourceBundle loadResourceBundle(Class auditorInterface) {
		String resourceName = ClassUtils.getPackageName(auditorInterface) + "/resources/" + ClassUtils.getShortClassName(auditorInterface).replace('.','$');
		LogFactory.getLog(getClass()).trace("try to load " + resourceName);
		return ResourceBundle.getBundle(resourceName);
	}

	static abstract class LogLevel {

		private static Map levels = new TreeMap();
		private final String name;

		LogLevel(String name) {
			this.name = name;
			levels.put(name, this);
		}

		public abstract boolean isEnabled(Log log);

		public abstract void log(Log log, String message);

		public static final LogLevel TRACE = new LogLevel("trace") {
			public boolean isEnabled(Log log) {
				return log.isTraceEnabled();
			}

			public void log(Log log, String message) {
				log.trace(message);
			}
		};

		public static final LogLevel DEBUG = new LogLevel("debug") {
			public boolean isEnabled(Log log) {
				return log.isDebugEnabled();
			}

			public void log(Log log, String message) {
				log.debug(message);
			}
		};

		public static final LogLevel INFO = new LogLevel("info") {
			public boolean isEnabled(Log log) {
				return log.isInfoEnabled();
			}

			public void log(Log log, String message) {
				log.info(message);
			}
		};

		public static final LogLevel WARN = new LogLevel("warn") {
			public boolean isEnabled(Log log) {
				return log.isWarnEnabled();
			}

			public void log(Log log, String message) {
				log.warn(message);
			}
		};


		public static final LogLevel ERROR = new LogLevel("error") {
			public boolean isEnabled(Log log) {
				return log.isErrorEnabled();
			}

			public void log(Log log, String message) {
				log.error(message);
			}
		};

		public static final LogLevel FATAL = new LogLevel("fatal") {
			public boolean isEnabled(Log log) {
				return log.isFatalEnabled();
			}

			public void log(Log log, String message) {
				log.fatal(message);
			}
		};

        public static LogLevel getInstance(String name) {
			LogLevel level = (LogLevel) levels.get(name);
			if (level == null) {
				return DEBUG;
			}
			return level;
		}

		public String toString() {
			return name;
		}


	}

	static class LogInvocationHandler extends AuditInvocationHandler {

		private final ResourceBundle bundle;
		private final Log log;

		public LogInvocationHandler(ResourceBundle bundle, Log log) {
			this.bundle = bundle;
			this.log = log;
		}

		private String createMessage(Method method, Object[] args) {
			String key = method.getName();
			MessageFormat format = new MessageFormat(bundle.getString(key));
			String message = format.format(args);
			return message;
		}

		protected void invokeAuditMethod(Method method, Object[] args) {
			LogFactory.getLog(getClass()).trace("log audit " + method + ArrayUtils.toString(args));

			LogLevel level;
			try {
				String levelName = bundle.getString(method.getName() + ".level");
				level = LogLevel.getInstance(levelName);
			} catch (Exception e) {
				level = LogLevel.DEBUG;
			}

			LogFactory.getLog(getClass()).trace("log level " + level);

			if (level.isEnabled(log)) {
				String message = createMessage(method, args);
				level.log(log, message);
			}
		}

	}

}
