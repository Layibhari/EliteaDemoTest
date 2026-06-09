/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.configuration.MutableConfiguration;

/**
 * Cache configuration class using JCache API.
 *
 * Enabled caching annotation scanning using @EnableCaching. Registers and customizes
 * JCache Manager instance configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
class CacheConfiguration {

	/**
	 * Configures custom cache attributes on application startup. Registers a cache named
	 * "vets" with basic customization settings.
	 * @return JCacheManagerCustomizer callback bean
	 */
	@Bean
	public JCacheManagerCustomizer petclinicCacheConfigurationCustomizer() {
		// Create the 'vets' cache with standard statistics-enabled configuration context
		return cm -> cm.createCache("vets", cacheConfiguration());
	}

	/**
	 * Create a simple configuration that enable statistics via the JCache programmatic
	 * configuration API.
	 * <p>
	 * Within the configuration object that is provided by the JCache API standard, there
	 * is only a very limited set of configuration options. The really relevant
	 * configuration options (like the size limit) must be set via a configuration
	 * mechanism that is provided by the selected JCache implementation.
	 * </p>
	 * @return JCache configuration parameters mapping
	 */
	private javax.cache.configuration.Configuration<Object, Object> cacheConfiguration() {
		// Enable statistics rendering for standard JMX monitoring reporting tools
		return new MutableConfiguration<>().setStatisticsEnabled(true);
	}

}
