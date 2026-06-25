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

import org.springframework.samples.petclinic.owner.OwnerNotFoundException;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Centralized exception handling for MVC controllers.
 *
 * Handles common runtime exceptions and maps them to the error view with appropriate HTTP
 * status codes and a message suitable for the error page.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(OwnerNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleOwnerNotFound(OwnerNotFoundException ex, Model model) {
		model.addAttribute("status", HttpStatus.NOT_FOUND.value());
		model.addAttribute("message", ex.getMessage());
		return "error";
	}

}
