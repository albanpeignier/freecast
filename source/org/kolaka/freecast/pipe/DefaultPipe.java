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

package org.kolaka.freecast.pipe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.packet.LogicalPage;
import org.kolaka.freecast.packet.LogicalPageBuilder;
import org.kolaka.freecast.packet.LogicalPageDescriptor;
import org.kolaka.freecast.packet.Packet;
import org.kolaka.freecast.packet.SequenceValidator;
import org.kolaka.freecast.timer.DefaultTimeBase;
import org.kolaka.freecast.timer.TimeBase;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class DefaultPipe implements Pipe {

	private final static int DEFAULT_CONSUMERQUEUELENGTH = 100;

	private final DefaultConsumer nextConsumer;

	private final Set consumers;

	private TimeBase timeBase = new DefaultTimeBase();

	public DefaultPipe() {
		this(DEFAULT_CONSUMERQUEUELENGTH);
	}

	public DefaultPipe(int consumerQueueLength) {
		this.consumerQueueLength = consumerQueueLength;

		nextConsumer = new DefaultConsumer(consumerQueueLength / 2);
		consumers = new HashSet();

		consumers.add(nextConsumer);
	}

	private int consumerQueueLength;

	public synchronized Consumer createConsumer(String name) {
		DefaultConsumer consumer = (DefaultConsumer) nextConsumer.clone();
		consumer.setName(name);
		consumer.setQueueLength(consumerQueueLength);
		consumers.add(consumer);
		return consumer;
	}

	class TimedLogicalPageBuilder extends LogicalPageBuilder {

		private final long creationTime;

		public TimedLogicalPageBuilder(LogicalPageDescriptor descriptor) {
			super(descriptor);
			creationTime = timeBase.currentTimeMillis();
		}

		public long getLiveTimeLength() {
			return timeBase.currentTimeMillis() - creationTime;
		}

	}

	class DefaultConsumer extends BaseConsumer implements Cloneable {

		private SequenceValidator validator = new SequenceValidator();

		private LogicalPage headerPage;

		private Map pageBuilders;

		private int queueLength;

		private String name;

		DefaultConsumer(int queueLength) {
			this.name = "default";
			this.queueLength = queueLength;
			headerPage = null;

			pageBuilders = new TreeMap();
		}

		public synchronized void push(Packet packet) {
			Long sequenceNumber = new Long(packet.getElementDescriptor()
					.getPageDescriptor().getSequenceNumber());
			LogicalPageBuilder builder = (LogicalPageBuilder) pageBuilders
					.get(sequenceNumber);

			if (builder == null) {
				builder = new TimedLogicalPageBuilder(packet
						.getElementDescriptor().getPageDescriptor());
				pageBuilders.put(sequenceNumber, builder);
			}

			if (this != nextConsumer) {
				// LogFactory.getLog(getClass()).debug("push packet: " +
				// sequenceNumber + "/" + packet.getSequenceNumber() + " - " +
				// pageBuilders.size());
			}
			builder.add(packet);

			if (pageBuilders.size() > (queueLength * 1.2)) {
				int initialBuilderCount = pageBuilders.size();
				for (Iterator iter = pageBuilders.values().iterator(); pageBuilders
						.size() > queueLength
						&& iter.hasNext();) {
					LogicalPageBuilder removedBuilder = (LogicalPageBuilder) iter
							.next();
					iter.remove();

					if (removedBuilder.isComplete()) {
						LogicalPage removedPage = removedBuilder.create();
						if (removedPage.isFirstPage()) {
							LogFactory.getLog(getClass()).debug(
									"cache headerpage " + removedPage);
							headerPage = removedPage;
						}
					} else {
						LogFactory.getLog(getClass()).debug(
								"remove incomplete builder " + removedBuilder);
					}
				}
				if (this != nextConsumer) {
					String message = "removed in " + name + " "
							+ (initialBuilderCount - pageBuilders.size())
							+ " builders";
					LogFactory.getLog(getClass()).debug(message);
				}
			}
		}

		public synchronized LogicalPage consume() throws EmptyPipeException {
			if (headerPage != null) {
				LogFactory.getLog(getClass()).debug(
						"Use cached first page: " + headerPage);

				LogicalPage returnedPage = headerPage;
				headerPage = null;
				return returnedPage;
			}

			Iterator iterator = pageBuilders.values().iterator();

			TimedLogicalPageBuilder builder = null;

			do {
				if (!iterator.hasNext()) {
					throw new EmptyPipeException();
				}

				builder = (TimedLogicalPageBuilder) iterator.next();

				if (!builder.isComplete() && !iterator.hasNext()
						&& builder.getLiveTimeLength() < 30000) {
					LogFactory.getLog(getClass()).debug(
							"single page incomplete kept: " + builder);
					throw new EmptyPipeException();
				}

				iterator.remove();

				if (!builder.isComplete()) {
					LogFactory.getLog(getClass()).debug(
							"incomplete page skipped: " + builder);
					builder = null;
				} else if (builder.getLiveTimeLength() > 120000) {
					LogFactory.getLog(getClass()).debug(
							"obsolete page skipped: " + builder);
					builder = null;
				}
			} while (builder == null);

			LogicalPage page = builder.create();
			validator.validate(page);

			return page;
		}

		protected void doClose() {
			consumers.remove(this);
		}

		public synchronized Object clone() {
			try {
				DefaultConsumer clone = (DefaultConsumer) super.clone();
				clone.pageBuilders = new TreeMap(pageBuilders);
				clone.validator = (SequenceValidator) validator.clone();
				return clone;
			} catch (CloneNotSupportedException e) {
				throw new UnhandledException(e);
			}
		}

		public void setQueueLength(int queueLength) {
			this.queueLength = queueLength;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String toString() {
			ToStringBuilder builder = new ToStringBuilder(this);
			builder.append("name", name);
			builder.append("pages.size", pageBuilders.size());
			builder.append("validator", validator);
			return builder.toString();
		}

	}

	private Producer producer;

	public Producer createProducer() {
		if (producer != null) {
			throw new NotImplementedException(
					"A single producer is supported for the moment");
		}
		producer = new DefaultProducer();
		return producer;
	}

	class DefaultProducer extends BaseProducer {

		private SequenceValidator validator = new SequenceValidator();

		public void push(Packet packet) {
			validator.validate(packet);
			for (Iterator iter = consumers.iterator(); iter.hasNext();) {
				DefaultConsumer consumer = (DefaultConsumer) iter.next();
				consumer.push(packet);
			}
		}

		protected void doClose() {
			producer = null;
		}

		public String toString() {
			ToStringBuilder builder = new ToStringBuilder(this);
			return builder.toString();
		}

	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}