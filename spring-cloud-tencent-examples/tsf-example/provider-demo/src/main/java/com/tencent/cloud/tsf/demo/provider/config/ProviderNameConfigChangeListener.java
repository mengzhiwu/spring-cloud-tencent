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

package com.tencent.cloud.tsf.demo.provider.config;

import com.tencent.tsf.consul.config.watch.ConfigChangeCallback;
import com.tencent.tsf.consul.config.watch.ConfigChangeListener;
import com.tencent.tsf.consul.config.watch.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
@ConfigChangeListener(prefix = "provider.config", value = {"name"})
public class ProviderNameConfigChangeListener implements ConfigChangeCallback {

	private static final Logger log = LoggerFactory.getLogger(ProviderNameConfigChangeListener.class);

	@Override
	public void callback(ConfigProperty lastConfigProperty, ConfigProperty newConfigProperty) {
		log.info("[TSF SDK] Configuration Change Listener: key: {}, old value: {}, new value: {}",
				lastConfigProperty.getKey(), lastConfigProperty.getValue(), newConfigProperty.getValue());
	}

}
