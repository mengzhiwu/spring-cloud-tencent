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

package com.tencent.cloud.lane.caller;

import java.util.HashMap;
import java.util.Map;

import com.tencent.cloud.common.spi.InstanceMetadataProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Custom metadata for instance.
 *
 * @author Haotian Zhang
 */
@Component
public class CustomMetadata implements InstanceMetadataProvider {

	@Value("${service.lane:base}")
	private String lane;

	@Override
	public Map<String, String> getMetadata() {
		Map<String, String> metadata = new HashMap<>();
		if (!"base".equals(lane)) {
			metadata.put("lane", lane);
		}
		return metadata;
	}
}
