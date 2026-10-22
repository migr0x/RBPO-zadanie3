package ru.mtusi.rbpo.lab3.model;

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

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "publication_available", nullable = false)
    private boolean publicationAvailable;

    protected EditorialSection() {
    }

    public EditorialSection(String name, boolean publicationAvailable) {
        this.name = name;
        this.publicationAvailable = publicationAvailable;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublicationAvailable() {
        return publicationAvailable;
    }

    public void setPublicationAvailable(boolean publicationAvailable) {
        this.publicationAvailable = publicationAvailable;
    }
}
