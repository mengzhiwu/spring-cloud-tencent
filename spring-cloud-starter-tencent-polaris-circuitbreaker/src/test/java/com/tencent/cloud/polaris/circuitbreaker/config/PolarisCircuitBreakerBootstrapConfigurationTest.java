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

package com.tencent.cloud.polaris.circuitbreaker.config;

import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import com.tencent.cloud.polaris.context.config.PolarisContextAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementAutoConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link PolarisCircuitBreakerBootstrapConfiguration}.
 *
 * @author Haotian Zhang
 */
public class PolarisCircuitBreakerBootstrapConfigurationTest {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					PolarisContextAutoConfiguration.class,
					RpcEnhancementAutoConfiguration.class,
					LoadBalancerAutoConfiguration.class,
					PolarisCircuitBreakerBootstrapConfiguration.class))
			.withPropertyValues("spring.cloud.polaris.enabled=true")
			.withPropertyValues("spring.cloud.polaris.circuitbreaker.enabled=true");

	@BeforeAll
	static void beforeAll() {
		PolarisSDKContextManager.innerDestroy();
	}

	@Test
	public void testDefaultInitialization() {
		this.contextRunner.run(context -> {
			assertThat(context).hasSingleBean(PolarisCircuitBreakerAutoConfiguration.class);
			assertThat(context).hasSingleBean(PolarisCircuitBreakerFeignClientAutoConfiguration.class);
			assertThat(context).hasSingleBean(ReactivePolarisCircuitBreakerAutoConfiguration.class);
		});
	}
}
