package org.springframework.samples.petclinic.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.samples.petclinic.dto.AuthRequest;
import org.springframework.samples.petclinic.dto.AuthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoint para emitir JWT tokens")
public class AuthenticationController {

	private final AuthenticationManager authenticationManager;

	private final UserDetailsService userDetailsService;

	private final JwtService jwtService;

	public AuthenticationController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
	}

	@Operation(summary = "Login para obtener el token JWT")
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
		authenticationManager
			.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
		final String jwt = jwtService.generateToken(userDetails);

		return ResponseEntity.ok(new AuthResponse(jwt));
	}

}
