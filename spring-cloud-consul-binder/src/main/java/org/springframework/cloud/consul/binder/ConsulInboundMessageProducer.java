/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.binder;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.consul.model.http.event.Event;
import org.springframework.integration.endpoint.MessageProducerSupport;

/**
 * Adapter that receives Messages from Consul Events, converts them into Spring
 * Integration Messages, and sends the results to a Message Channel.
 *
 * @author Spencer Gibb
 */
public class ConsulInboundMessageProducer extends MessageProducerSupport {

	protected static final Log logger = LogFactory.getLog(ConsulInboundMessageProducer.class);

	private final ScheduledExecutorService scheduler;

	private final Runnable eventsRunnable;

	private EventService eventService;

	private ScheduledFuture<?> eventsHandle;

	public ConsulInboundMessageProducer(EventService eventService) {
		this.eventService = eventService;
		this.scheduler = Executors.newScheduledThreadPool(1);
		this.eventsRunnable = new Runnable() {

			@Override
			public void run() {
				getEvents();
			}
		};
	}

	// link eventService to sendMessage
	/*
	 * Map<String, Object> headers =
	 * headerMapper.toHeadersFromRequest(message.getMessageProperties()); if
	 * (messageListenerContainer.getAcknowledgeMode() == AcknowledgeMode.MANUAL) {
	 * headers.put(AmqpHeaders.DELIVERY_TAG,
	 * message.getMessageProperties().getDeliveryTag()); headers.put(AmqpHeaders.CHANNEL,
	 * channel); }
	 * sendMessage(AmqpInboundChannelAdapter.this.getMessageBuilderFactory().withPayload
	 * (payload).copyHeaders(headers).build());
	 */

	// start thread
	// make blocking calls
	// foreach event -> send message

	@Override
	protected void doStart() {
		// TODO: make configurable
		this.eventsHandle = this.scheduler.scheduleWithFixedDelay(this.eventsRunnable, 500, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void doStop() {
		if (this.eventsHandle != null) {
			this.eventsHandle.cancel(true);
		}
		this.scheduler.shutdown();
	}

	// @Scheduled(fixedDelayString = "${spring.cloud.consul.binder.eventDelay:30000}")
	public void getEvents() {
		try {
			List<Event> events = this.eventService.watch();
			for (Event event : events) {
				// Map<String, Object> headers = new HashMap<>();
				// headers.put(MessageHeaders.REPLY_CHANNEL, outputChannel.)
				String decoded = new String(Base64.getDecoder().decode(event.getPayload()));
				sendMessage(getMessageBuilderFactory().withPayload(decoded)
					// TODO: support headers
					.build());
			}
		}
		catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Error getting consul events: " + e.getMessage());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Error getting consul events", e);
			}
		}
	}

}
