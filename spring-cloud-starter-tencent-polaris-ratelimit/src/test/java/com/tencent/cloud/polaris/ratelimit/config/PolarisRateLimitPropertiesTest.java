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

package com.tencent.cloud.polaris.ratelimit.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link PolarisRateLimitProperties}.
 *
 * @author Haotian Zhang
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PolarisRateLimitPropertiesTest.TestApplication.class, properties = "spring.application.name=test")
@ActiveProfiles("test")
public class PolarisRateLimitPropertiesTest {

	@Autowired
	private PolarisRateLimitProperties polarisRateLimitProperties;

	@Test
	public void testDefaultInitialization() {
		assertThat(polarisRateLimitProperties).isNotNull();
		assertThat(polarisRateLimitProperties.getRejectRequestTips()).isEqualTo("xxx");
		assertThat(polarisRateLimitProperties.getRejectRequestTipsFilePath()).isEqualTo("/index.html");
		assertThat(polarisRateLimitProperties.getRejectHttpCode()).isEqualTo(419);
		assertThat(polarisRateLimitProperties.getMaxQueuingTime()).isEqualTo(500L);
	}

	@SpringBootApplication
	protected static class TestApplication {

	}
}
