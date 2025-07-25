/*
 * Copyright 2013-2025 the original author or authors.
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

package org.springframework.cloud.consul.support;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.ReregistrationPredicate;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.consul.serviceregistry.ActuatorHealthApplicationStatusProvider;
import org.springframework.cloud.consul.serviceregistry.ApplicationStatusProvider;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for the heartbeat.
 *
 * @author Tim Ysewyn
 * @author Chris Bono
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnConsulEnabled
@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
@ConditionalOnDiscoveryEnabled
@AutoConfigureBefore({ ConsulServiceRegistryAutoConfiguration.class })
@AutoConfigureAfter({ ConsulDiscoveryClientConfiguration.class })
public class ConsulHeartbeatAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public TtlScheduler ttlScheduler(HeartbeatProperties heartbeatProperties,
			ConsulDiscoveryProperties discoveryProperties, ConsulClient consulClient,
			ReregistrationPredicate reRegistrationPredicate,
			ObjectProvider<ApplicationStatusProvider> applicationStatusProvider) {
		return new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient, reRegistrationPredicate,
				applicationStatusProvider);
	}

	@Bean
	@ConditionalOnMissingBean
	public ReregistrationPredicate reRegistrationPredicate() {
		return ReregistrationPredicate.DEFAULT;
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(HealthEndpoint.class)
	@ConditionalOnProperty(value = "spring.cloud.consul.discovery.heartbeat.use-actuator-health", havingValue = "true",
			matchIfMissing = true)
	static class ActuatorBasedApplicationStatusProviderConfig {

		@Bean
		@ConditionalOnBean(HealthEndpoint.class)
		@ConditionalOnMissingBean
		public ApplicationStatusProvider actuatorHealthStatusProvider(HealthEndpoint healthEndpoint,
				HeartbeatProperties heartbeatProperties) {
			return new ActuatorHealthApplicationStatusProvider(healthEndpoint, heartbeatProperties);
		}

	}

}
