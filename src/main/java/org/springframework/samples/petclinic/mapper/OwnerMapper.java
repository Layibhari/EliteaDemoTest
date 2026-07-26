package org.springframework.samples.petclinic.mapper;

import java.util.stream.Collectors;

import org.springframework.samples.petclinic.dto.OwnerDTO;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.stereotype.Component;

@Component
public class OwnerMapper {

	private final PetMapper petMapper;

	public OwnerMapper(PetMapper petMapper) {
		this.petMapper = petMapper;
	}

	public OwnerDTO toDto(Owner entity) {
		if (entity == null) {
			return null;
		}
		OwnerDTO dto = new OwnerDTO();
		dto.setId(entity.getId());
		dto.setFirstName(entity.getFirstName());
		dto.setLastName(entity.getLastName());
		dto.setAddress(entity.getAddress());
		dto.setCity(entity.getCity());
		dto.setTelephone(entity.getTelephone());

		if (entity.getPets() != null) {
			dto.setPets(entity.getPets().stream().map(petMapper::toDto).collect(Collectors.toList()));
		}

		return dto;
	}

	public Owner toEntity(OwnerDTO dto) {
		if (dto == null) {
			return null;
		}
		Owner owner = new Owner();
		owner.setId(dto.getId());
		owner.setFirstName(dto.getFirstName());
		owner.setLastName(dto.getLastName());
		owner.setAddress(dto.getAddress());
		owner.setCity(dto.getCity());
		owner.setTelephone(dto.getTelephone());

		// Note: We don't map pets from DTO to Entity here to avoid unintended detached
		// entity issues.
		// Pets should be managed through PetService or individually.
		return owner;
	}

}
