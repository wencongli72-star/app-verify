package com.example.appverify.controller;

import com.example.appverify.dto.LicenseVerifyRequest;
import com.example.appverify.dto.LicenseVerifyResponse;
import com.example.appverify.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * GET /license/generate
     * 生成100条随机code（duration=15D）
     *
     * @return
     */
    @GetMapping("/generate")
    public ResponseEntity<String> generate100() {
        licenseService.generateCodes(100);
        return ResponseEntity.ok("success");
    }

    /**
     * GET /license/generate
     * 生成100条随机code（duration=15D）
     *
     * @return
     */
    @GetMapping("/test")
    public ResponseEntity<String> v() {
        licenseService.test();
        return ResponseEntity.ok("success");
    }
}
