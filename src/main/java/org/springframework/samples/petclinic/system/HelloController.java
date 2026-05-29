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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health-check controller that returns a fixed JSON greeting.
 * <p>
 * Requires no authentication and no database access, making it safe to call
 * from CI pipelines or load-balancer health probes to confirm the application
 * is running and serialising JSON correctly.
 */
@RestController
class HelloController {

	record HelloResponse(String message) {
	}

	@GetMapping("/hello")
	HelloResponse hello() {
		return new HelloResponse("hello, this is for testing purpose");
	}

}
