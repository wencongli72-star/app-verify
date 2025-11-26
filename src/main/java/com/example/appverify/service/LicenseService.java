package com.example.appverify.service;

import com.example.appverify.dto.LicenseVerifyResponse;
import com.example.appverify.entity.Device;
import com.example.appverify.entity.LicenseCode;
import com.example.appverify.repository.DeviceRepository;
import com.example.appverify.repository.LicenseCodeRepository;
import com.example.appverify.util.DurationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseCodeRepository licenseRepo;
    private final DeviceRepository deviceRepo;

    @Transactional
    public LicenseVerifyResponse verify(String code, String deviceId) {
        if (code == null || code.isBlank()) {
            return build(false, "invalid", null, "无效的使用码");
        }

        Optional<LicenseCode> opt = licenseRepo.findByCode(code);
        if (opt.isEmpty()) {
            return build(false, "invalid", null, "无效的使用码");
        }
        LicenseCode license = opt.get();

        // 如果已经到期 -> 删除使用码并返回 expired
        if (license.getActivationTime()!=null&&DurationUtil.isExpired(license.getActivationTime(), license.getDuration())) {
            // 计算 expireAt（用于响应）
            String expireAt = DurationUtil.computeExpireAtIsoUtc(license.getActivationTime(), license.getDuration());
            // 如果没有设置 ON DELETE CASCADE，需要先删除 devices
            try {
                // 若使用 FK ON DELETE CASCADE，这里可以直接 delete
                licenseRepo.delete(license);
                // 若没有 cascade，请启用下面一行（已在 DeviceRepository 提供）
                // deviceRepo.deleteByCode(code);
            } catch (Exception ex) {
                // 记录日志但仍返回 expired
                // logger.warn("删除到期使用码失败 code=" + code, ex);
            }
            return build(false, "expired", expireAt, "使用码已过期并已移除");
        }

        // 使用码绑定检查
        Optional<LicenseCode> boundByCodeOpt = licenseRepo.findLicenseCodeByCodeAndActiveIsTrue(code);
        if (boundByCodeOpt.isPresent()&&!boundByCodeOpt.get().getCode().equals(code)) {
            // 计算 expireAt（用于响应）
            String expireAt = DurationUtil.computeExpireAtIsoUtc(license.getActivationTime(), license.getDuration());
            return build(false, "device_mismatch", expireAt, "此使用码已绑定其他设备");
        } else {
            // 进行绑定
            Device newDevice = new Device();
            newDevice.setDeviceId(deviceId);
            newDevice.setCode(code);
            newDevice.setBoundAt(LocalDateTime.now());
            deviceRepo.save(newDevice);
            // 激活：如果 active 为 false，说明是首次使用 -> 激活并保存
            if (Boolean.FALSE.equals(license.getActive())) {
                license.setActivationTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
                license.setActive(true);
                licenseRepo.save(license);
            }
        }
        // 计算 expireAt（用于响应）
        String expireAt = DurationUtil.computeExpireAtIsoUtc(license.getActivationTime(), license.getDuration());

        return build(true, "valid", expireAt, "订阅有效");
    }

    private LicenseVerifyResponse build(boolean ok, String status, String expireAt, String message) {
        return LicenseVerifyResponse.builder()
                .ok(ok)
                .status(status)
                .expireAt(expireAt)
                .message(message)
                .build();
    }

    public void generateCodes(int count) {
        List<LicenseCode> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            LicenseCode code = new LicenseCode();
            code.setCode(generateRandomCode());
            code.setStartTime(LocalDateTime.now());
            code.setActive(true);
            code.setDuration("15D");

            list.add(code);
        }

        licenseRepo.saveAll(list);
    }

    // 随机生成 12 位唯一 code
    private String generateRandomCode() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    public void test() {
        System.out.println("=== 完整时区诊断 ===");

        // 1. 各种时间获取方式
        System.out.println("1. LocalDateTime.now(): " + LocalDateTime.now());
        System.out.println("2. Instant.now(): " + Instant.now());
        System.out.println("3. ZonedDateTime.now(): " + ZonedDateTime.now());
        System.out.println("4. ZonedDateTime上海: " + ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
        System.out.println("5. LocalDateTime上海: " + LocalDateTime.now(ZoneId.of("Asia/Shanghai")));

        // 2. 时区信息
        System.out.println("6. 默认时区: " + ZoneId.systemDefault());
        System.out.println("7. JVM默认时区: " + TimeZone.getDefault());
        System.out.println("8. 所有上海相关时区: " +
                        ZoneId.getAvailableZoneIds().stream()
                                .filter(z -> z.toLowerCase().contains("shanghai"))
                                .findFirst().orElse("Not Found"));



                // 3. 系统属性
                System.out.println("9. user.timezone: " + System.getProperty("user.timezone"));
        System.out.println("10. 当前时间戳: " + System.currentTimeMillis());

    }

}


