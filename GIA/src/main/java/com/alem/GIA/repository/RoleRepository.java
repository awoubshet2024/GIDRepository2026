package com.alem.GIA.repository;

import com.alem.GIA.DTO.RoleDto;
import com.alem.GIA.iModel.RolePermissionProjection;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {


    @Query("SELECT u FROM ApplicationUser u JOIN FETCH u.roles WHERE u.userName = :username")
    Optional<ApplicationUser> findByUsernameWithRoles(@Param("username") String username);
    Optional<Role> findRoleByRoleName(String username);
    //boolean existsByUserName(String userName);
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();
    // Query with users and permissions
    @Query("SELECT DISTINCT r FROM Role r " +
            "LEFT JOIN FETCH r.permissions " +
            "LEFT JOIN FETCH r.users")
    List<Role> findAllWithUsersAndPermissions();
    Optional<Role> findByRoleName(String roleName);





}

