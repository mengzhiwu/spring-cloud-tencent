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

package com.tencent.cloud.polaris.circuitbreaker.common;

import com.tencent.cloud.common.constant.OrderConstant;
import com.tencent.cloud.polaris.context.PolarisConfigModifier;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementReporterProperties;
import com.tencent.polaris.api.config.consumer.ServiceRouterConfig;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import com.tencent.polaris.plugins.router.healthy.RecoverRouterConfig;

/**
 * CircuitBreakerConfigModifier.
 *
 * @author seanyu 2023-02-27
 */
public class CircuitBreakerConfigModifier implements PolarisConfigModifier {

	private final RpcEnhancementReporterProperties properties;

	public CircuitBreakerConfigModifier(RpcEnhancementReporterProperties properties) {
		this.properties = properties;
	}

	@Override
	public void modify(ConfigurationImpl configuration) {
		properties.setEnabled(true);

		// Turn on circuitbreaker configuration
		configuration.getConsumer().getCircuitBreaker().setEnable(true);

		// Set excludeCircuitBreakInstances to true
		RecoverRouterConfig recoverRouterConfig = configuration.getConsumer().getServiceRouter()
				.getPluginConfig(ServiceRouterConfig.DEFAULT_ROUTER_RECOVER, RecoverRouterConfig.class);

		recoverRouterConfig.setExcludeCircuitBreakInstances(true);

		// Update modified config to source properties
		configuration.getConsumer().getServiceRouter()
				.setPluginConfig(ServiceRouterConfig.DEFAULT_ROUTER_RECOVER, recoverRouterConfig);
	}

	@Override
	public int getOrder() {
		return OrderConstant.Modifier.CIRCUIT_BREAKER_ORDER;
	}
}
