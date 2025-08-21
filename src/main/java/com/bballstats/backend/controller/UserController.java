package com.bballstats.backend.controller;

import com.bballstats.backend.model.User;
import com.bballstats.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200") // Angular lokalno
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public List<User> all() {
        return users.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> byId(@PathVariable Long id) {
        return users.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User saved = users.save(user);
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!users.existsById(id)) return ResponseEntity.notFound().build();
        users.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User req) {
        return users.findById(id)
                .map(u -> {
                    u.setUsername(req.getUsername());
                    u.setEmail(req.getEmail());
                    u.setPasswordHash(req.getPasswordHash()); // privremeno plain; hash ćemo kasnije
                    u.setRole(req.getRole());
                    User saved = users.save(u);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
