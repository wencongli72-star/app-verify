package com.example.appverify.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// LicenseCode.java
@Entity
@Table(name = "license_codes")
@Data
public class LicenseCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private String duration; // e.g. "1H", "30M", "7D"
}