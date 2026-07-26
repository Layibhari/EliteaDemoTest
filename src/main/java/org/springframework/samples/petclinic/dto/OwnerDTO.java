package org.springframework.samples.petclinic.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class OwnerDTO {

	private Integer id;

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	@NotBlank
	private String address;

	@NotBlank
	private String city;

	@NotBlank
	@Digits(fraction = 0, integer = 10)
	private String telephone;

	private List<PetDTO> pets = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public List<PetDTO> getPets() {
		return pets;
	}

	public void setPets(List<PetDTO> pets) {
		this.pets = pets;
	}

}
