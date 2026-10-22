package org.example.lab3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.lab3.models.UserRole;

public record UserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull UserRole role) {
}
