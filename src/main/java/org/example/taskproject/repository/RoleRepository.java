package org.example.taskproject.repository;


import org.example.taskproject.entity.RoleEntity;
import org.example.taskproject.enums.RoleName;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName name);
}
