package com.bballstats.backend.dto.auth;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String role; // "USER" ili "ADMIN"
}
