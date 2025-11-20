package com.example.appverify.scheduler;

import com.example.appverify.entity.LicenseCode;
import com.example.appverify.repository.DeviceRepository;
import com.example.appverify.repository.LicenseCodeRepository;
import com.example.appverify.util.DurationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseExpiryScheduler {

    private final LicenseCodeRepository licenseRepo;
    private final DeviceRepository deviceRepo;

    // 每 30 分钟检查一次（根据需要调整）
    @Scheduled(cron = "0 0/30 * * * ?")
    @Transactional
    public void scanAndDeleteExpired() {
        List<LicenseCode> actives = licenseRepo.findByActiveTrue();
        if (actives.isEmpty()) return;

        List<LicenseCode> toDelete = new ArrayList<>();
        for (LicenseCode lc : actives) {
            try {
                if (DurationUtil.isExpired(lc.getStartTime(), lc.getDuration())) {
                    toDelete.add(lc);
                }
            } catch (Exception ex) {
                // logger.warn("duration parse failed for code=" + lc.getCode(), ex);
            }
        }

        if (!toDelete.isEmpty()) {
            // 如果没有外键 ON DELETE CASCADE，需要先删除 devices 手动清理
            for (LicenseCode lc : toDelete) {
                String code = lc.getCode();
                // 如果使用 FK/ON DELETE CASCADE，这行可注释掉
                deviceRepo.deleteByCode(code);
            }
            // 批量删除 license
            licenseRepo.deleteAllInBatch(toDelete);
            // logger.info("Deleted expired licenses: " + toDelete.size());
        }
    }
}
