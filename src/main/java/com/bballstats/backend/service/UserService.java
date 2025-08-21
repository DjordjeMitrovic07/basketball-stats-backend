package com.bballstats.backend.service;

import com.bballstats.backend.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    User create(User user);
    Optional<User> update(Long id, User updated);
    boolean delete(Long id);
}
