package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.dto.PetDTO;
import org.springframework.samples.petclinic.dto.PetTypeDTO;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

	public PetDTO toDto(Pet entity) {
		if (entity == null) {
			return null;
		}
		PetDTO dto = new PetDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setBirthDate(entity.getBirthDate());

		if (entity.getType() != null) {
			PetTypeDTO typeDto = new PetTypeDTO();
			typeDto.setId(entity.getType().getId());
			typeDto.setName(entity.getType().getName());
			dto.setType(typeDto);
		}

		return dto;
	}

	public Pet toEntity(PetDTO dto) {
		if (dto == null) {
			return null;
		}
		Pet pet = new Pet();
		pet.setId(dto.getId());
		pet.setName(dto.getName());
		pet.setBirthDate(dto.getBirthDate());

		if (dto.getType() != null) {
			PetType type = new PetType();
			type.setId(dto.getType().getId());
			type.setName(dto.getType().getName());
			pet.setType(type);
		}
		return pet;
	}

	public PetTypeDTO toDto(PetType entity) {
		if (entity == null) {
			return null;
		}
		PetTypeDTO dto = new PetTypeDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		return dto;
	}

}
