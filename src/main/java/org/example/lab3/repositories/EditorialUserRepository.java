package org.example.lab3.repositories;

import org.example.lab3.models.EditorialUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EditorialUserRepository extends JpaRepository<EditorialUser, Long> {
}
