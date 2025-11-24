package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {
    private String nom;
    private String email;
    private String role;
}
