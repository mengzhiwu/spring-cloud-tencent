/*
 * Tencent is pleased to support the open source community by making spring-cloud-tencent available.
 *
 * Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.cloud.polaris.discovery;

import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import com.tencent.cloud.polaris.context.config.PolarisContextAutoConfiguration;
import com.tencent.polaris.test.mock.discovery.NamingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static com.tencent.polaris.test.common.Consts.PORT;
import static com.tencent.polaris.test.common.Consts.SERVICE_PROVIDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link PolarisDiscoveryClientConfiguration}.
 *
 * @author Haotian Zhang
 */
public class PolarisDiscoveryClientConfigurationTest {

	private static NamingServer namingServer;

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					PolarisContextAutoConfiguration.class,
					PolarisDiscoveryClientConfiguration.class,
					PolarisContextAutoConfiguration.class))
			.withPropertyValues("spring.application.name=" + SERVICE_PROVIDER)
			.withPropertyValues("server.port=" + PORT)
			.withPropertyValues("spring.cloud.polaris.address=grpc://127.0.0.1:10081");

	@BeforeAll
	static void beforeAll() throws Exception {
		namingServer = NamingServer.startNamingServer(10081);
	}

	@AfterAll
	static void afterAll() {
		if (null != namingServer) {
			namingServer.terminate();
		}
	}

	@BeforeEach
	void setUp() {
		PolarisSDKContextManager.innerDestroy();
	}

	@Test
	public void testDefaultInitialization() {
		this.contextRunner.run(context -> assertThat(context).hasSingleBean(PolarisDiscoveryClient.class));
	}

	@Test
	public void testDiscoveryBlockingDisabled() {
		this.contextRunner.withPropertyValues("spring.cloud.discovery.blocking.enabled=false")
				.run(context -> assertThat(context).doesNotHaveBean(PolarisDiscoveryClient.class));
	}

	@Configuration
	@EnableAutoConfiguration
	static class PolarisDiscoveryClientConfiguration {

	}
}
