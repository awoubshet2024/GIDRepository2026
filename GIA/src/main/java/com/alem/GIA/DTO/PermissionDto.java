package com.alem.GIA.DTO;

import com.alem.GIA.enumes.PermissionName;
import com.alem.GIA.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "permissionId")
public class PermissionDto {
   private Integer permissionId;
  private  String permissionName;
  //private Set<RoleDto>roles;

    public PermissionDto(String permissionName){
        this.permissionName = permissionName;
    }
   /* public PermissionDto(Integer permissionId, String permissionName){
        this.permissionName = permissionName;
        this.permissionId = permissionId;
    }*/
    public static PermissionDto fromEntity(Permission permission){
        return new PermissionDto(permission.getId(), permission.getPermissionName());

    }
}
