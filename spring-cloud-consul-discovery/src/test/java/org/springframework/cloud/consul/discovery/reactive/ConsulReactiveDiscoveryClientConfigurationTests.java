/*
 * Copyright 2019-2025 the original author or authors.
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

package org.springframework.cloud.consul.discovery.reactive;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.ReactiveCommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.client.discovery.health.reactive.ReactiveDiscoveryClientHealthIndicator;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tim Ysewyn
 */
class ConsulReactiveDiscoveryClientConfigurationTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(UtilAutoConfiguration.class, ReactiveCommonsClientAutoConfiguration.class,
					ConsulAutoConfiguration.class, ConsulReactiveDiscoveryClientConfiguration.class));

	@Test
	public void shouldWorkWithDefaults() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(ReactiveDiscoveryClient.class);
			assertThat(context).hasSingleBean(ReactiveDiscoveryClientHealthIndicator.class);
		});
	}

	@Test
	public void shouldNotHaveDiscoveryClientWhenDiscoveryDisabled() {
		contextRunner.withPropertyValues("spring.cloud.discovery.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean("consulReactiveDiscoveryClient");
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClient.class);
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClientHealthIndicator.class);
		});
	}

	@Test
	public void shouldNotHaveDiscoveryClientWhenReactiveDiscoveryDisabled() {
		contextRunner.withPropertyValues("spring.cloud.discovery.reactive.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean("consulReactiveDiscoveryClient");
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClient.class);
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClientHealthIndicator.class);
		});
	}

	@Test
	public void shouldNotHaveDiscoveryClientWhenConsulDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean("consulReactiveDiscoveryClient");
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClient.class);
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClientHealthIndicator.class);
		});
	}

	@Test
	public void shouldNotHaveDiscoveryClientWhenConsulDiscoveryDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.discovery.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean("consulReactiveDiscoveryClient");
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClient.class);
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClientHealthIndicator.class);
		});
	}

	@Test
	public void worksWithoutWebflux() {
		contextRunner.withClassLoader(new FilteredClassLoader("org.springframework.web.reactive")).run(context -> {
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClient.class);
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClientHealthIndicator.class);
		});
	}

	@Test
	public void worksWithoutHealth() {
		contextRunner.withClassLoader(new FilteredClassLoader("org.springframework.boot.health")).run(context -> {
			assertThat(context).hasSingleBean(ReactiveDiscoveryClient.class);
			assertThat(context).doesNotHaveBean(ReactiveDiscoveryClientHealthIndicator.class);
		});
	}

}
