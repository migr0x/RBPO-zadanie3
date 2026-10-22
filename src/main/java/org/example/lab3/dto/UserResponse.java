package org.example.lab3.dto;

import org.example.lab3.models.UserRole;

public record UserResponse(Long id, String name, UserRole role) {
}
