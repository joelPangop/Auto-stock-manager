package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDto {
    private String email;
    private String motDePasse;
}
