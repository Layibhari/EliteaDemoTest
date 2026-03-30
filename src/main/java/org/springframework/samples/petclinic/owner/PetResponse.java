package org.springframework.samples.petclinic.owner;

public class PetResponse {
	private String name;
	private String type;

	public PetResponse(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}
