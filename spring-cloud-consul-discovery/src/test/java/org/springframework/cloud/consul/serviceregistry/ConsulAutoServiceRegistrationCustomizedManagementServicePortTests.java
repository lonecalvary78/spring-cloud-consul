/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Aleksandr Tarasov (aatarasov)
 * @author Alex Antonov (aantonov)
 * @author Lomesh Patel (lomeshpatel)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationCustomizedManagementServicePortTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-GG",
				"spring.cloud.consul.discovery.instanceId=myTestService1-GG",
				"spring.cloud.consul.discovery.registerHealthCheck=false",
				"spring.cloud.consul.discovery.managementPort=4452",
				"spring.cloud.consul.discovery.serviceName=myprefix-${spring.application.name}",
				"management.server.port=0" },
		webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulAutoServiceRegistrationCustomizedManagementServicePortTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Autowired
	private ManagementServerProperties managementServerProperties;

	@Test
	public void contextLoads() {
		final Response<Map<String, Service>> response = this.consul.getAgentServices();
		final Map<String, Service> services = response.getValue();

		final Service service = services.get("myTestService1-GG");
		assertThat(service).as("service was null").isNotNull();
		assertThat(service.getPort().intValue()).as("service port was 0").isNotEqualTo(0);
		assertThat(service.getId()).as("service id was wrong").isEqualTo("myTestService1-GG");
		assertThat(service.getService()).as("service name was wrong").isEqualTo("myprefix-myTestService-GG");
		assertThat(ObjectUtils.isEmpty(service.getAddress())).as("service address must not be empty").isFalse();
		assertThat(service.getAddress()).as("service address must equals hostname from discovery properties")
			.isEqualTo(this.discoveryProperties.getHostname());

		final Service managementService = services.get("myTestService1-GG-management");
		assertThat(managementService).as("management service was null").isNotNull();
		assertThat(managementService.getPort().intValue()).as("management service port is not 4452").isEqualTo(4452);
		assertThat(this.managementServerProperties.getPort().intValue()).as("management port is not 0").isEqualTo(0);
		assertThat(managementService.getId()).as("management service id was wrong")
			.isEqualTo("myTestService1-GG-management");
		assertThat(managementService.getService()).as("management service name was wrong")
			.isEqualTo("myprefix-myTestService-GG-management");
		assertThat(ObjectUtils.isEmpty(managementService.getAddress()))
			.as("management service address must not be empty")
			.isFalse();
		assertThat(managementService.getAddress())
			.as("management service address must equals hostname from discovery properties")
			.isEqualTo(this.discoveryProperties.getHostname());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
