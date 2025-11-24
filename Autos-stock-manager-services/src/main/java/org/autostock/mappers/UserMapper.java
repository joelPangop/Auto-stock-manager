package org.autostock.mappers;

import org.autostock.dtos.UserCreateDto;
import org.autostock.dtos.UserDto;
import org.autostock.dtos.UserListDto;
import org.autostock.dtos.UserUpdateDto;
import org.autostock.enums.Role;
import org.autostock.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateDto dto, String motDePasseHash) {
        User u = new User();
        u.setNom(dto.getNom());
        u.setEmail(dto.getEmail());
        u.setMotDePasseHash(motDePasseHash);
        u.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        return u;
    }

    public UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setNom(u.getNom());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole().name());
        dto.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
        dto.setUpdatedAt(u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null);
        return dto;
    }

    public UserListDto toListDto(User u) {
        UserListDto dto = new UserListDto();
        dto.setId(u.getId());
        dto.setNom(u.getNom());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole().name());
        return dto;
    }

    public void updateEntity(User u, UserUpdateDto dto) {
        u.setNom(dto.getNom());
        u.setEmail(dto.getEmail());
        u.setRole(Role.valueOf(dto.getRole().toUpperCase()));
    }
}
