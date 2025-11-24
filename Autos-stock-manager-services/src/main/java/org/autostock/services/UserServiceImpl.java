package org.autostock.services;
import org.autostock.enums.Role;
import org.autostock.models.User;
import org.autostock.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl  extends AbstractBaseService<User, Long, UserRepository>
        implements UserService {

    @Override
    @Transactional(readOnly = true)
    public Optional<User> trouverParEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> trouverParRole(Role role) {
        return repository.findByRole(role);
    }
}
