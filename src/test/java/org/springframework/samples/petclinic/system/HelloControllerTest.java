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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link WebMvcTest} slice tests for {@link HelloController}.
 * <p>
 * Verifies the 200 happy-path (status, Content-Type, exact JSON body) and the
 * 405 Method Not Allowed responses for POST, PUT, and DELETE.
 */
@WebMvcTest(HelloController.class)
@DisabledInNativeImage
@DisabledInAotMode
class HelloControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getHello_returns200WithJsonGreeting() throws Exception {
		mockMvc.perform(get("/hello"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(content().json("{\"message\":\"hello, this is for testing purpose\"}", true));
	}

	@Test
	void postHello_returns405() throws Exception {
		mockMvc.perform(post("/hello")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	void putHello_returns405() throws Exception {
		mockMvc.perform(put("/hello")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	void deleteHello_returns405() throws Exception {
		mockMvc.perform(delete("/hello")).andExpect(status().isMethodNotAllowed());
	}

}
