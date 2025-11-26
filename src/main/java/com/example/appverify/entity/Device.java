package com.example.appverify.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// Device.java
@Entity
@Table(name = "devices")
@Data
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String code;

    @Column(name = "bound_at")
    private LocalDateTime boundAt;
}