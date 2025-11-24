package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserListDto {
    private Long id;
    private String nom;
    private String email;
    private String role;
}
