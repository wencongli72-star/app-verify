package com.example.appverify.dto;

import lombok.Data;

// LicenseVerifyRequest.java
@Data
public class LicenseVerifyRequest {
    private String code;
    private String deviceId;
}
