package com.dsabg.dsabgauthservice.dto.request;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotNull
    private String username;

    @NotNull
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    private String phone;

    @NotNull
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
