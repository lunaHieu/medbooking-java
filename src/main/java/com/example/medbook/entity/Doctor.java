package com.example.medbook.entity;

import jakarta.persistence.*;

import javax.print.Doc;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @Column(name = "DoctorID")
    private int doctorId;
    @Column(name = "Degree")
    private String degree;
    @Column(name = "YearsOfExperience")
    private int yearsOfExperience;
    @Column(name = "ImageURL")
    private String imageURL;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "DoctorID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecialtyID", nullable = false)
    private Specialty specialty;

    @Column(name = "ProfileDescription")
    private String profileDescription;

    public Doctor(){}
    public Doctor(int doctorId, String degree, int yearsOfExperience, String imageURL, User user, Specialty specialty, String profileDescription) {
        this.doctorId = doctorId;
        this.degree = degree;
        this.yearsOfExperience = yearsOfExperience;
        this.imageURL = imageURL;
        this.user = user;
        this.specialty = specialty;
        this.profileDescription = profileDescription;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }
}
