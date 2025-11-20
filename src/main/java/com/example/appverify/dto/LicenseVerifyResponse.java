package com.example.appverify.dto;

import lombok.Builder;
import lombok.Data;

// LicenseVerifyResponse.java
@Data
@Builder
public class LicenseVerifyResponse {
    private boolean ok;
    private String status;  // valid, expired, invalid, device_mismatch
    private String expireAt;
    private String message;
}
