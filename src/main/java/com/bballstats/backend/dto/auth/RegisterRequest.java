package com.bballstats.backend.dto.auth;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password; // plain text u requestu, hashiramo u servisu
}
