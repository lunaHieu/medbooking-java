package com.example.medbook.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties")
public class Specialty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SpecialtyID")
    private int specialtyId;
    @Column(name = "SpecialtyName", nullable = false)
    private String specialtyName;
    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "ImageURL")
    private String imageURL;
    public Specialty() {}
    public Specialty(int specialtyId, String specialtyName, String description, String imageURL) {
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.description = description;
        this.imageURL = imageURL;
    }

    public int getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(int specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName) {
        this.specialtyName = specialtyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
