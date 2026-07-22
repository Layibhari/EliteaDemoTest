/*
 * Copyright 2012-2026 the original author or authors.
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

package org.springframework.samples.petclinic.config;

import java.time.Duration;
import java.util.Set;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Cache configuration that supports an external Redis cache (profile 'redis') and a local
 * Caffeine fallback (default / when 'redis' profile is not active).
 */
@Configuration(proxyBeanMethods = false)
public class CacheConfig {

	@Configuration(proxyBeanMethods = false)
	@Profile("redis")
	static class RedisCacheConfig {

		@Bean
		public LettuceConnectionFactory redisConnectionFactory(org.springframework.core.env.Environment env) {
			String host = env.getProperty("spring.redis.host", "localhost");
			int port = Integer.parseInt(env.getProperty("spring.redis.port", "6379"));
			// LettuceConnectionFactory will be configured from spring.redis.* properties
			return new LettuceConnectionFactory(host, port);
		}

		@Bean
		public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
			RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(60));
			return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(config)
				.initialCacheNames(Set.of("vets"))
				.build();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Profile("!redis")
	static class LocalCacheConfig {

		@Bean
		public CaffeineCacheManager cacheManager() {
			CaffeineCacheManager cacheManager = new CaffeineCacheManager("vets");
			cacheManager.setCaffeine(Caffeine.newBuilder().maximumSize(1000));
			return cacheManager;
		}

	}

}
