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

package org.springframework.tsf.core.util;

import com.tencent.cloud.common.util.ApplicationContextAwareUtils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Spring context utils.
 * <p>
 * Deprecated since 2.0.0.0.
 *
 * @author hongweizhu
 */
@Deprecated
public class TsfSpringContextAware {

	/**
	 * Get application context.
	 * @return application context
	 */
	public static ApplicationContext getApplicationContext() {
		return ApplicationContextAwareUtils.getApplicationContext();
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// do nothing.
	}

	/**
	 * Get application property.
	 * @param key property name
	 * @return property value
	 */
	public static String getProperties(String key) {
		return ApplicationContextAwareUtils.getProperties(key);
	}

	/**
	 * Get application property. If null, return default.
	 * @param key property name
	 * @param defaultValue default value
	 * @return property value
	 */
	public static String getProperties(String key, String defaultValue) {
		return ApplicationContextAwareUtils.getProperties(key, defaultValue);
	}

	public static <T> T getBean(Class<T> requiredType) {
		return ApplicationContextAwareUtils.getBean(requiredType);
	}
}
