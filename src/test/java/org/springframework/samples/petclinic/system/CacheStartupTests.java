/*
 * Minimal smoke test to verify cache configuration loads with local (Caffeine) profile
 */

package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class CacheStartupTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void cacheManagerIsCaffeineAndVetsCacheExists() {
		CacheManager cacheManager = context.getBean(CacheManager.class);
		assertThat(cacheManager).isNotNull();
		assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
		assertThat(cacheManager.getCache("vets")).isNotNull();
	}

}
