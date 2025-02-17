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

import com.tencent.cloud.polaris.circuitbreaker.instrument.feign.PolarisCircuitBreakerNameResolver;
import com.tencent.cloud.polaris.circuitbreaker.instrument.feign.PolarisFeignCircuitBreaker;
import com.tencent.cloud.polaris.circuitbreaker.instrument.feign.PolarisFeignCircuitBreakerTargeter;
import feign.Feign;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.cloud.openfeign.Targeter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * PolarisCircuitBreakerFeignClientAutoConfiguration.
 *
 * @author seansyyu 2023-02-28
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Feign.class, FeignClientFactoryBean.class})
@ConditionalOnPolarisCircuitBreakerEnabled
public class PolarisCircuitBreakerFeignClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(CircuitBreakerNameResolver.class)
	public CircuitBreakerNameResolver polarisCircuitBreakerNameResolver() {
		return new PolarisCircuitBreakerNameResolver();
	}

	@Bean
	@ConditionalOnBean(CircuitBreakerFactory.class)
	@ConditionalOnMissingBean(Targeter.class)
	public Targeter polarisFeignCircuitBreakerTargeter(CircuitBreakerFactory circuitBreakerFactory, CircuitBreakerNameResolver circuitBreakerNameResolver) {
		return new PolarisFeignCircuitBreakerTargeter(circuitBreakerFactory, circuitBreakerNameResolver);
	}

	@Bean
	@Scope("prototype")
	@ConditionalOnBean(CircuitBreakerFactory.class)
	@ConditionalOnMissingBean(Feign.Builder.class)
	public Feign.Builder circuitBreakerFeignBuilder() {
		return PolarisFeignCircuitBreaker.builder();
	}

}
