package com.example.appverify.controller;

import com.example.appverify.dto.LicenseVerifyRequest;
import com.example.appverify.dto.LicenseVerifyResponse;
import com.example.appverify.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseController {
    private final LicenseService licenseService;

    @PostMapping("/verify")
    public ResponseEntity<LicenseVerifyResponse> verify(@RequestBody LicenseVerifyRequest req) {
        LicenseVerifyResponse resp = licenseService.verify(req.getCode(), req.getDeviceId());
        return ResponseEntity.ok(resp);
    }
}
