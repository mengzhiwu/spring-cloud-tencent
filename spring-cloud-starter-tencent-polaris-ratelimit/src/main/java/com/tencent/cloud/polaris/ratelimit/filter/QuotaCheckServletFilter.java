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

package com.tencent.cloud.polaris.ratelimit.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import com.tencent.cloud.common.constant.HeaderConstant;
import com.tencent.cloud.common.constant.OrderConstant;
import com.tencent.cloud.common.metadata.MetadataContext;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitProperties;
import com.tencent.cloud.polaris.ratelimit.spi.PolarisRateLimiterLimitedFallback;
import com.tencent.cloud.polaris.ratelimit.utils.QuotaCheckUtils;
import com.tencent.cloud.polaris.ratelimit.utils.RateLimitUtils;
import com.tencent.polaris.api.pojo.RetStatus;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.assembly.api.AssemblyAPI;
import com.tencent.polaris.ratelimit.api.core.LimitAPI;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResponse;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResultCode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.filter.OncePerRequestFilter;

import static com.tencent.cloud.common.constant.ContextConstant.UTF_8;

/**
 * Servlet filter to check quota.
 *
 * @author Haotian Zhang, lepdou, cheese8
 */
@Order(OrderConstant.Server.Servlet.RATE_LIMIT_FILTER_ORDER)
public class QuotaCheckServletFilter extends OncePerRequestFilter {

	/**
	 * Default Filter Registration Bean Name Defined .
	 */
	public static final String QUOTA_FILTER_BEAN_NAME = "quotaFilterRegistrationBean";
	private static final Logger LOG = LoggerFactory.getLogger(QuotaCheckServletFilter.class);
	private final LimitAPI limitAPI;

	private final AssemblyAPI assemblyAPI;

	private final PolarisRateLimitProperties polarisRateLimitProperties;

	private final PolarisRateLimiterLimitedFallback polarisRateLimiterLimitedFallback;

	private String rejectTips;

	public QuotaCheckServletFilter(LimitAPI limitAPI, AssemblyAPI assemblyAPI,
			PolarisRateLimitProperties polarisRateLimitProperties,
			@Nullable PolarisRateLimiterLimitedFallback polarisRateLimiterLimitedFallback) {
		this.limitAPI = limitAPI;
		this.assemblyAPI = assemblyAPI;
		this.polarisRateLimitProperties = polarisRateLimitProperties;
		this.polarisRateLimiterLimitedFallback = polarisRateLimiterLimitedFallback;
	}

	@PostConstruct
	public void init() {
		rejectTips = RateLimitUtils.getRejectTips(polarisRateLimitProperties);
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String localNamespace = MetadataContext.LOCAL_NAMESPACE;
		String localService = MetadataContext.LOCAL_SERVICE;
		QuotaResponse quotaResponse = null;
		try {
			quotaResponse = QuotaCheckUtils.getQuota(limitAPI, localNamespace, localService, 1, request.getRequestURI());
			if (quotaResponse.getCode() == QuotaResultCode.QuotaResultLimited) {
				if (Objects.nonNull(quotaResponse.getActiveRule())
						&& StringUtils.isNotBlank(quotaResponse.getActiveRule().getCustomResponse().getBody())) {
					response.setStatus(polarisRateLimitProperties.getRejectHttpCode());
					response.setContentType("text/plain;charset=UTF-8");
					response.getWriter().write(quotaResponse.getActiveRule().getCustomResponse().getBody());
				}
				else if (!Objects.isNull(polarisRateLimiterLimitedFallback)) {
					response.setStatus(polarisRateLimiterLimitedFallback.rejectHttpCode());
					String contentType = new MediaType(polarisRateLimiterLimitedFallback.mediaType(), polarisRateLimiterLimitedFallback.charset()).toString();
					response.setContentType(contentType);
					response.getWriter().write(polarisRateLimiterLimitedFallback.rejectTips());
				}
				else {
					response.setStatus(polarisRateLimitProperties.getRejectHttpCode());
					response.setContentType("text/html;charset=UTF-8");
					response.getWriter().write(rejectTips);
				}
				// set flow control to header
				response.addHeader(HeaderConstant.INTERNAL_CALLEE_RET_STATUS, RetStatus.RetFlowControl.getDesc());
				// set trace span
				RateLimitUtils.reportTrace(assemblyAPI, quotaResponse.getActiveRule().getId().getValue());
				if (Objects.nonNull(quotaResponse.getActiveRule())) {
					try {
						String encodedActiveRuleName = URLEncoder.encode(
								quotaResponse.getActiveRuleName(), UTF_8);
						response.addHeader(HeaderConstant.INTERNAL_ACTIVE_RULE_NAME, encodedActiveRuleName);
					}
					catch (UnsupportedEncodingException e) {
						LOG.error("Cannot encode {} for header internal-callee-activerule.",
								quotaResponse.getActiveRuleName(), e);
					}
				}
				RateLimitUtils.release(quotaResponse);
				return;
			}
			// Unirate
			if (quotaResponse.getCode() == QuotaResultCode.QuotaResultOk && quotaResponse.getWaitMs() > 0) {
				LOG.debug("The request of [{}] will waiting for {}ms.", request.getRequestURI(), quotaResponse.getWaitMs());
				Thread.sleep(quotaResponse.getWaitMs());
			}

		}
		catch (Throwable t) {
			// An exception occurs in the rate limiting API call,
			// which should not affect the call of the business process.
			LOG.error("fail to invoke getQuota, service is " + localService, t);
		}

		try {
			filterChain.doFilter(request, response);
		}
		finally {
			RateLimitUtils.release(quotaResponse);
		}
	}

}
