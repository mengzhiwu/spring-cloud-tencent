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

package com.tencent.cloud.polaris.circuitbreaker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.tencent.cloud.polaris.circuitbreaker.common.PolarisCircuitBreakerConfigBuilder;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerProperties;
import com.tencent.cloud.polaris.circuitbreaker.util.PolarisCircuitBreakerUtils;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.utils.ThreadPoolUtils;
import com.tencent.polaris.circuitbreak.api.CircuitBreakAPI;
import com.tencent.polaris.client.util.NamedThreadFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

/**
 * PolarisCircuitBreakerFactory.
 *
 * @author seanyu 2023-02-27
 */
public class PolarisCircuitBreakerFactory
		extends CircuitBreakerFactory<PolarisCircuitBreakerConfigBuilder.PolarisCircuitBreakerConfiguration, PolarisCircuitBreakerConfigBuilder> implements DisposableBean {

	private final CircuitBreakAPI circuitBreakAPI;

	private final ConsumerAPI consumerAPI;

	private final ScheduledExecutorService cleanupService = Executors.newSingleThreadScheduledExecutor(
			new NamedThreadFactory("sct-circuitbreaker-cleanup", true));

	private Function<String, PolarisCircuitBreakerConfigBuilder.PolarisCircuitBreakerConfiguration> defaultConfiguration =
			id -> {
				String[] metadata = PolarisCircuitBreakerUtils.resolveCircuitBreakerId(id);
				return new PolarisCircuitBreakerConfigBuilder()
						.namespace(metadata[0])
						.service(metadata[1])
						.path(metadata[2])
						.protocol(metadata[3])
						.method(metadata[4])
						.build();
			};

	public PolarisCircuitBreakerFactory(CircuitBreakAPI circuitBreakAPI, ConsumerAPI consumerAPI,
			PolarisCircuitBreakerProperties polarisCircuitBreakerProperties) {
		this.circuitBreakAPI = circuitBreakAPI;
		this.consumerAPI = consumerAPI;
		cleanupService.scheduleWithFixedDelay(
				() -> {
					getConfigurations().clear();
				},
				polarisCircuitBreakerProperties.getConfigurationCleanupInterval(),
				polarisCircuitBreakerProperties.getConfigurationCleanupInterval(), TimeUnit.MILLISECONDS);
	}

	@Override
	public CircuitBreaker create(String id) {
		PolarisCircuitBreakerConfigBuilder.PolarisCircuitBreakerConfiguration conf = getConfigurations()
				.computeIfAbsent(id, defaultConfiguration);
		return new PolarisCircuitBreaker(conf, consumerAPI, circuitBreakAPI);
	}

	@Override
	protected PolarisCircuitBreakerConfigBuilder configBuilder(String id) {
		String[] metadata = PolarisCircuitBreakerUtils.resolveCircuitBreakerId(id);
		return new PolarisCircuitBreakerConfigBuilder(metadata[0], metadata[1], metadata[2], metadata[3], metadata[4]);
	}

	@Override
	public void configureDefault(Function<String, PolarisCircuitBreakerConfigBuilder.PolarisCircuitBreakerConfiguration> defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	@Override
	public void destroy() {
		ThreadPoolUtils.waitAndStopThreadPools(new ExecutorService[] {cleanupService});
	}

}
