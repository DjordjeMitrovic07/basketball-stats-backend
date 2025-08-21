package com.bballstats.backend.service.impl;

import com.bballstats.backend.model.User;
import com.bballstats.backend.repository.UserRepository;
import com.bballstats.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<User> findAll() {
        return repo.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public User create(User user) {
        return repo.save(user);
    }

    @Override
    public Optional<User> update(Long id, User updated) {
        return repo.findById(id).map(existing -> {
            existing.setUsername(updated.getUsername());
            existing.setEmail(updated.getEmail());
            existing.setPasswordHash(updated.getPasswordHash());
            existing.setRole(updated.getRole());
            return repo.save(existing);
        });
    }

    @Override
    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
