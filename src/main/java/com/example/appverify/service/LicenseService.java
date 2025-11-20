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

import java.time.LocalDateTime;
import java.util.Optional;

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

        // 计算 expireAt（用于响应）
        String expireAt = null;
        try {
            expireAt = DurationUtil.computeExpireAtIsoUtc(license.getStartTime(), license.getDuration());
        } catch (Exception e) {
            // duration 格式异常 -> 当作无效处理
            return build(false, "invalid", null, "使用码格式错误");
        }

        // 如果已经到期 -> 删除使用码并返回 expired
        if (DurationUtil.isExpired(license.getStartTime(), license.getDuration())) {
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

        // 激活：如果 active 为 false，说明是首次使用 -> 激活并保存
        if (Boolean.FALSE.equals(license.getActive())) {
            license.setActive(true);
            licenseRepo.save(license);
        }

        // 设备绑定检查
        Optional<Device> boundByCodeOpt = deviceRepo.findByCode(code);
        if (boundByCodeOpt.isPresent()) {
            Device bound = boundByCodeOpt.get();
            if (!bound.getDeviceId().equals(deviceId)) {
                return build(false, "device_mismatch", expireAt, "此使用码已绑定其他设备");
            }
            // 相同 deviceId -> 通过
        } else {
            // 尚未绑定，检查 deviceId 是否已绑定其他 code
            Optional<Device> existingDevice = deviceRepo.findByDeviceId(deviceId);
            if (existingDevice.isPresent()) {
                return build(false, "device_mismatch", expireAt, "此设备已绑定其他使用码");
            }
            // 进行绑定
            Device newDevice = new Device();
            newDevice.setDeviceId(deviceId);
            newDevice.setCode(code);
            newDevice.setBoundAt(LocalDateTime.now());
            deviceRepo.save(newDevice);
        }

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
}
