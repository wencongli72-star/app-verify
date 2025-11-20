package com.example.appverify.repository;

import com.example.appverify.entity.Device;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceId(String deviceId);
    Optional<Device> findByCode(String code);
    // 兼容方案：如果没有 ON DELETE CASCADE，可用下面的方法在删除 license 前清设备绑定
    @Modifying
    @Transactional
    @Query("DELETE FROM Device d WHERE d.code = :code")
    void deleteByCode(@Param("code") String code);
}