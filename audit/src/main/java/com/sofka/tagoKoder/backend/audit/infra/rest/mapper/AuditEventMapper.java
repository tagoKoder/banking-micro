package com.sofka.tagoKoder.backend.audit.infra.rest.mapper;

import com.sofka.tagoKoder.backend.audit.domain.model.AuditEvent;
import com.sofka.tagoKoder.backend.audit.infra.config.MappingConfig;
import com.sofka.tagoKoder.backend.audit.infra.persistence.jpa.AuditEventEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = MappingConfig.class)
public interface AuditEventMapper {

  // Entidad JPA -> Dominio
  AuditEvent toDomain(AuditEventEntity entity);

  // Dominio -> Entidad JPA
  @Mapping(target = "id", ignore = false) // lo seteas tú en dominio
  AuditEventEntity toEntity(AuditEvent domain);

  // Update (si lo necesitas en el futuro)
  @BeanMapping(ignoreByDefault = false)
  void updateEntity(AuditEvent domain, @MappingTarget AuditEventEntity entity);

  // Batch
  List<AuditEvent> toDomainList(List<AuditEventEntity> entities);
  List<AuditEventEntity> toEntityList(List<AuditEvent> domains);
}
