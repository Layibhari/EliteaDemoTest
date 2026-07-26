package org.springframework.samples.petclinic.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public class PetDTO {

	private Integer id;

	@NotBlank
	private String name;

	@NotNull
	@PastOrPresent
	private LocalDate birthDate;

	@NotNull
	private PetTypeDTO type;

	private Integer ownerId;

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

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public PetTypeDTO getType() {
		return type;
	}

	public void setType(PetTypeDTO type) {
		this.type = type;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

}
