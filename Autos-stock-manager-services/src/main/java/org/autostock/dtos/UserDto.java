package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String nom;
    private String email;
    private String role;
    private String createdAt;
    private String updatedAt;
}
