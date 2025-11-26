package com.example.appverify.repository;

import com.example.appverify.entity.LicenseCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseCodeRepository extends JpaRepository<LicenseCode, Long> {
    Optional<LicenseCode> findByCode(String code);
    List<LicenseCode> findByActiveTrue();
    Optional<LicenseCode> findLicenseCodeByCodeAndActiveIsTrue(String code);
}