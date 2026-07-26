package org.springframework.samples.petclinic.dto;

import jakarta.validation.constraints.NotBlank;

public class PetTypeDTO {

	private Integer id;

	@NotBlank
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
