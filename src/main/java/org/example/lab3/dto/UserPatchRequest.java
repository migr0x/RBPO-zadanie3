package org.example.lab3.dto;

import jakarta.validation.constraints.Size;
import org.example.lab3.models.UserRole;

public record UserPatchRequest(
        @Size(min = 1, max = 100) String name,
        UserRole role) {
}
