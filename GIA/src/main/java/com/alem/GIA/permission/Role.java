package com.alem.GIA.permission;


import com.alem.GIA.enumes.RoleName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String roleName;
    @ManyToMany(mappedBy = "roles",fetch=FetchType.EAGER)
    @JsonIgnore
    private Set<ApplicationUser> users = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role(String roleName){
        this.roleName = roleName;
    }

   public Role(String roleName,Set<Permission>permissions){
        this.roleName = roleName;
        this.permissions = permissions;
   }


   /* public void addUserToRole(ApplicationUser user){
        user.getRoles().add(this);
        this.getUsers().add(user);



    }*/
 /*   public void addPermissionToRole(Permission permission){
        permission.getRoles().add(this);
        this.getPermissions().add(permission);


    }*/



}
