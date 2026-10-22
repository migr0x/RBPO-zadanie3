package org.example.lab3.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "editorial_sections")
public class EditorialSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "available_for_publication", nullable = false)
    private boolean availableForPublication;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailableForPublication() {
        return availableForPublication;
    }

    public void setAvailableForPublication(boolean availableForPublication) {
        this.availableForPublication = availableForPublication;
    }
}
