package com.bballstats.backend.service;

import com.bballstats.backend.dto.auth.*;
import com.bballstats.backend.model.Role;
import com.bballstats.backend.model.User;
import com.bballstats.backend.repository.UserRepository;
import com.bballstats.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;   // ✔ sad se bean nalazi
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthResponse register(RegisterRequest req){
        if (userRepo.existsByEmail(req.getEmail())){
            throw new IllegalArgumentException("Email already in use");
        }

        User u = User.builder()
                .email(req.getEmail())
                .username(req.getEmail())                  // ✔ koristimo email i kao username
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();

        u = userRepo.save(u);

        UserDetails ud = org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities("ROLE_" + u.getRole().name())
                .build();

        String token = jwt.generateToken(ud);

        return AuthResponse.builder()
                .token(token)
                .userId(u.getId())
                .email(u.getEmail())
                .role(u.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest req){
        // login po email-u (može i username, jer UserDetailsService podržava oba)
        authManager.authenticate(new UsernamePasswordAuthenticationToken(
                req.getEmail(), req.getPassword()
        ));

        User u = userRepo.findByEmail(req.getEmail()).orElseThrow();

        UserDetails ud = org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities("ROLE_" + u.getRole().name())
                .build();

        String token = jwt.generateToken(ud);

        return AuthResponse.builder()
                .token(token)
                .userId(u.getId())
                .email(u.getEmail())
                .role(u.getRole().name())
                .build();
    }
}
