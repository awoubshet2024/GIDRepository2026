package com.alem.GIA.repository;

import com.alem.GIA.permission.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Integer> {


    Optional<Permission> findPermissionByPermissionName(String name);
  Optional<Permission>  findByPermissionName(String name);
    @Query("SELECT p FROM Permission p LEFT JOIN FETCH p.roles WHERE p.id = :id")
    Optional<Permission> findWithRolesById(@Param("id") Integer id);

}
