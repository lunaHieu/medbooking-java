package com.example.medbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
@Getter
@Setter
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceID")
    private Integer serviceId;
    @Column(name = "ServiceName", nullable = false)
    private String serviceName;
    @Column(name = "Description",columnDefinition = "TEXT")
    private String description;
    @Column(name = "EstimatedDuration")
    private Integer estimatedDuration;
    @Column(name = "Price", precision = 18, scale = 2)
    private BigDecimal price;
    @Column(name = "ImageURL")
    private String imageURL;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecialtyID")
    private Specialty specialty;
    public Service() {}
    public Service(int  serviceId, String serviceName, String description, BigDecimal price, String imageURL, Specialty specialty) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.imageURL = imageURL;
        this.specialty = specialty;
    }
}

