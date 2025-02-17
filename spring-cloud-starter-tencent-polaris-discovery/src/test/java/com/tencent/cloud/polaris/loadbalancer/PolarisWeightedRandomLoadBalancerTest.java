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

package com.tencent.cloud.polaris.loadbalancer;

import java.util.ArrayList;
import java.util.List;

import com.tencent.cloud.common.pojo.PolarisServiceInstance;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.polaris.api.exception.ErrorCode;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.router.api.core.RouterAPI;
import com.tencent.polaris.router.api.rpc.ProcessLoadBalanceResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import static com.tencent.cloud.common.metadata.MetadataContext.LOCAL_NAMESPACE;
import static com.tencent.cloud.common.metadata.MetadataContext.LOCAL_SERVICE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link PolarisWeightedRandomLoadBalancer}.
 *
 * @author rod.xu
 */
@ExtendWith(MockitoExtension.class)
public class PolarisWeightedRandomLoadBalancerTest {

	private static MockedStatic<ApplicationContextAwareUtils> mockedApplicationContextAwareUtils;
	private static Instance testInstance;
	@Mock
	private RouterAPI routerAPI;
	@Mock
	private ObjectProvider<ServiceInstanceListSupplier> supplierObjectProvider;

	@BeforeAll
	static void beforeAll() {
		mockedApplicationContextAwareUtils = Mockito.mockStatic(ApplicationContextAwareUtils.class);
		mockedApplicationContextAwareUtils.when(() -> ApplicationContextAwareUtils.getProperties(anyString()))
				.thenReturn("unit-test");

		testInstance = Instance.createDefaultInstance("instance-id", LOCAL_NAMESPACE,
				LOCAL_SERVICE, "host", 8090);
	}

	@AfterAll
	static void afterAll() {
		mockedApplicationContextAwareUtils.close();
	}

	@Test
	public void chooseNormalLogicTest_thenReturnAvailablePolarisInstance() {

		Request request = Mockito.mock(Request.class);
		List<ServiceInstance> mockInstanceList = new ArrayList<>();
		mockInstanceList.add(new PolarisServiceInstance(testInstance));

		ServiceInstanceListSupplier serviceInstanceListSupplier = Mockito.mock(ServiceInstanceListSupplier.class);
		when(serviceInstanceListSupplier.get(request)).thenReturn(Flux.just(mockInstanceList));

		when(supplierObjectProvider.getIfAvailable(any())).thenReturn(serviceInstanceListSupplier);

		ProcessLoadBalanceResponse mockLbRes = new ProcessLoadBalanceResponse(testInstance);
		when(routerAPI.processLoadBalance(any())).thenReturn(mockLbRes);

		// request construct and execute invoke
		PolarisWeightedRandomLoadBalancer polarisWeightedRandomLoadBalancer = new PolarisWeightedRandomLoadBalancer(LOCAL_SERVICE, supplierObjectProvider, routerAPI);
		Mono<Response<ServiceInstance>> responseMono = polarisWeightedRandomLoadBalancer.choose(request);
		ServiceInstance serviceInstance = responseMono.block().getServer();

		// verify method has invoked
		verify(supplierObjectProvider).getIfAvailable(any());

		//result assert
		Assertions.assertThat(serviceInstance).isNotNull();
		Assertions.assertThat(serviceInstance instanceof PolarisServiceInstance).isTrue();

		PolarisServiceInstance polarisServiceInstance = (PolarisServiceInstance) serviceInstance;

		Assertions.assertThat(polarisServiceInstance.getPolarisInstance().getId()).isEqualTo("instance-id");
		Assertions.assertThat(polarisServiceInstance.getPolarisInstance().getNamespace()).isEqualTo(LOCAL_NAMESPACE);
		Assertions.assertThat(polarisServiceInstance.getPolarisInstance().getService()).isEqualTo(LOCAL_SERVICE);
		Assertions.assertThat(polarisServiceInstance.getPolarisInstance().getHost()).isEqualTo("host");
		Assertions.assertThat(polarisServiceInstance.getPolarisInstance().getPort()).isEqualTo(8090);
	}

	@Test
	public void chooseExceptionTest_thenReturnEmptyInstance() {

		Request request = Mockito.mock(Request.class);
		List<ServiceInstance> mockInstanceList = new ArrayList<>();
		mockInstanceList.add(new PolarisServiceInstance(testInstance));

		ServiceInstanceListSupplier serviceInstanceListSupplier = Mockito.mock(ServiceInstanceListSupplier.class);
		when(serviceInstanceListSupplier.get(request)).thenReturn(Flux.just(mockInstanceList));

		when(supplierObjectProvider.getIfAvailable(any())).thenReturn(serviceInstanceListSupplier);

		when(routerAPI.processLoadBalance(any())).thenThrow(new PolarisException(ErrorCode.API_TIMEOUT));

		// request construct and execute invoke
		PolarisWeightedRandomLoadBalancer polarisWeightedRandomLoadBalancer = new PolarisWeightedRandomLoadBalancer(LOCAL_SERVICE, supplierObjectProvider, routerAPI);
		Mono<Response<ServiceInstance>> responseMono = polarisWeightedRandomLoadBalancer.choose(request);
		ServiceInstance serviceInstance = responseMono.block().getServer();

		// verify method has invoked
		verify(supplierObjectProvider).getIfAvailable(any());

		//result assert
		Assertions.assertThat(serviceInstance).isNull();
	}

	@Test
	public void chooseEmptySupplierTest_thenReturnEmptyInstance() {
		ServiceInstanceListSupplier noopSupplier = new NoopServiceInstanceListSupplier();
		when(supplierObjectProvider.getIfAvailable(any())).thenReturn(noopSupplier);

		// request construct and execute invoke
		PolarisWeightedRandomLoadBalancer polarisWeightedRandomLoadBalancer = new PolarisWeightedRandomLoadBalancer(LOCAL_SERVICE, supplierObjectProvider, routerAPI);
		Mono<Response<ServiceInstance>> responseMono = polarisWeightedRandomLoadBalancer.choose();
		ServiceInstance serviceInstance = responseMono.block().getServer();

		//result assert
		Assertions.assertThat(serviceInstance).isNull();
	}

}
