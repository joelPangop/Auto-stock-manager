package org.autostock.services;

import org.autostock.enums.Role;
import org.autostock.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends IService<User, Long> {

    Optional<User> trouverParEmail(String email);

    List<User> trouverParRole(Role role);
}
