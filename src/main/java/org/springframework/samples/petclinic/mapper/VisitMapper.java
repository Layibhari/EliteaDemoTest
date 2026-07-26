package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.dto.VisitDTO;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.stereotype.Component;

@Component
public class VisitMapper {

	public VisitDTO toDto(Visit entity) {
		if (entity == null) {
			return null;
		}
		VisitDTO dto = new VisitDTO();
		dto.setId(entity.getId());
		dto.setDate(entity.getDate());
		dto.setDescription(entity.getDescription());
		return dto;
	}

	public Visit toEntity(VisitDTO dto) {
		if (dto == null) {
			return null;
		}
		Visit visit = new Visit();
		visit.setId(dto.getId());
		visit.setDate(dto.getDate());
		visit.setDescription(dto.getDescription());
		return visit;
	}

}
