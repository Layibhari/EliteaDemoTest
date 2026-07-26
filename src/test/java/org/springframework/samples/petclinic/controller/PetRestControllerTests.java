package org.springframework.samples.petclinic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.samples.petclinic.security.JwtService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PetRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserDetailsService userDetailsService;

	@Test
	public void shouldReturn401WhenNotAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/pets?ownerId=1&petId=1")).andExpect(status().isUnauthorized());
	}

	@Test
	public void shouldReturnOkOrNotFoundWhenAuthenticated() throws Exception {
		UserDetails user = userDetailsService.loadUserByUsername("admin");
		String token = jwtService.generateToken(user);
		mockMvc
			.perform(MockMvcRequestBuilders.get("/api/pets?ownerId=1&petId=1")
				.header("Authorization", "Bearer " + token))
			.andExpect(result -> {
				int status = result.getResponse().getStatus();
				assert status == 200 || status == 404;
			});
	}

}
