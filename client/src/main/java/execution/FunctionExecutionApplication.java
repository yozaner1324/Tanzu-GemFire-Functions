/*
Copyright (C) 2019-Present Pivotal Software, Inc. All rights reserved.

This program and the accompanying materials are made available under the terms of the under the Apache License, Version
2.0 (the "License”); you may not use this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
 */

package execution;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;

@SpringBootApplication
// Causes the creation of server-side Cloud Cache/GemFire regions via the @Cacheable annotation during the
// Initialization phase of the app's lifecycle
public class FunctionExecutionApplication {

	@Configuration
	@EnableClusterConfiguration(useHttp = true, requireHttps = false)
	static class LocalConfiguration {
		@Bean("Numbers")
		protected ClientRegionFactoryBean<Long, Long> configureProxyClientCustomerRegion(GemFireCache gemFireCache) {
			ClientRegionFactoryBean<Long, Long> clientRegionFactoryBean = new ClientRegionFactoryBean<>();
			clientRegionFactoryBean.setCache(gemFireCache);
			clientRegionFactoryBean.setName("Numbers");
			clientRegionFactoryBean.setShortcut(ClientRegionShortcut.PROXY);
			return clientRegionFactoryBean;
		}
	}

	@Bean
	public ApplicationRunner runner(@Qualifier("Numbers") Region<Long, Long> region) {
		return args -> {
			region.putIfAbsent(0L, 10L);
			region.putIfAbsent(1L, 10L);
			region.putIfAbsent(2L, 4L);
			region.putIfAbsent(3L, 7L);
			region.putIfAbsent(4L, 11L);
			region.putIfAbsent(5L, 8L);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(FunctionExecutionApplication.class, args);
	}
}
