package com.alem.GIA.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemDto {

    private String title;
    private String path;
    private String icon;
    private String requiredPermission;
    private List<MenuItemDto> children;

    // Constructors
    public MenuItemDto(String title, String path, String icon, String requiredPermission) {
        this.title = title;
        this.path = path;
        this.icon = icon;
        this.requiredPermission =  requiredPermission;
        this.children = new ArrayList<>();
    }
}
