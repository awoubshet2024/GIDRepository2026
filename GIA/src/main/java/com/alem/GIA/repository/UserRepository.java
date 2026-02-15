package com.alem.GIA.repository;

import com.alem.GIA.permission.ApplicationUser;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<ApplicationUser,Integer> {

      Optional<ApplicationUser> findByUserName(String username);
      boolean existsByUserName(String userName);

      Optional<ApplicationUser> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM ApplicationUser u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions p")
    Set<ApplicationUser> findAllWithRolesAndPermissions();
     @EntityGraph(attributePaths = {"roles", "roles.permissions"})
      List<ApplicationUser> findAll();


    Optional<ApplicationUser> findByResetToken(String token);
    @Query("""
    SELECT COUNT(u)
    FROM ApplicationUser u
    JOIN u.roles r
    WHERE r.roleName = 'ADMIN'
""")
    long countUsersWithAdminRole();
    boolean existsByRoles_RoleName(String roleName);

    @Query("""
    SELECT COUNT(u)
    FROM ApplicationUser u
    JOIN u.roles r
    WHERE r.roleName = 'ADMIN'
      AND u.id <> :userId
""")
    long countOtherAdmins(@Param("userId") Integer userId);

}
