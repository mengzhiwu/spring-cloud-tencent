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

package com.tencent.cloud.polaris.router.config;

import com.tencent.cloud.common.metadata.config.MetadataAutoConfiguration;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.cloud.polaris.context.config.PolarisContextAutoConfiguration;
import com.tencent.cloud.polaris.router.instrument.feign.RouterLabelFeignInterceptor;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * test for {@link FeignAutoConfiguration}.
 * @author dongyinuo
 */
public class FeignAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					MetadataAutoConfiguration.class,
					RouterAutoConfiguration.class,
					RouterConfigModifierAutoConfiguration.class,
					PolarisContextAutoConfiguration.class,
					FeignAutoConfiguration.class,
					ApplicationContextAwareUtils.class
			)).withPropertyValues("spring.application.name=test");

	@Test
	public void routerLabelInterceptor() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(FeignAutoConfiguration.class);
			assertThat(context).hasSingleBean(RouterLabelFeignInterceptor.class);
		});
	}

}
