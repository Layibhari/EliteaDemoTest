package org.springframework.samples.petclinic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.samples.petclinic.security.JwtService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OwnerRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserDetailsService userDetailsService;

	@Test
	public void shouldReturn401WhenNotAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/owners")).andExpect(status().isUnauthorized());
	}

	@Test
	public void shouldReturn200WhenAuthenticated() throws Exception {
		UserDetails user = userDetailsService.loadUserByUsername("admin");
		String token = jwtService.generateToken(user);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/owners").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());
	}

	@Test
	public void shouldAllowPublicAccessToThymeleafControllers() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/owners")).andExpect(status().isOk());
	}

}
