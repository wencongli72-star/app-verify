package com.example.appverify.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DurationUtil {

    /**
     * 解析像 "1H", "30M", "7D", "90S" 这样的字符串为 java.time.Duration
     * 支持 H(小时)、M(分钟)、S(秒)、D(天)
     */
    public static Duration parseSimpleDuration(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("duration is null/empty");
        }
        s = s.trim().toUpperCase();
        // 正则简单校验：数字+单位
        if (!s.matches("\\d+[HMSD]")) {
            throw new IllegalArgumentException("unsupported duration format: " + s);
        }
        long value = Long.parseLong(s.substring(0, s.length() - 1));
        char unit = s.charAt(s.length() - 1);
        switch (unit) {
            case 'H':
                return Duration.ofHours(value);
            case 'M':
                return Duration.ofMinutes(value);
            case 'S':
                return Duration.ofSeconds(value);
            case 'D':
                return Duration.ofDays(value);
            default:
                throw new IllegalArgumentException("unsupported duration unit: " + unit);
        }
    }

    /**
     * 计算 expireAt：startTime + duration
     * 返回 UTC Instant 对应的 ISO-8601 字符串 (例如: 2025-12-31T23:59:59Z)
     */
    public static String computeExpireAtIsoUtc(LocalDateTime startTime, String durationStr) {
        Duration d = parseSimpleDuration(durationStr);
        // 假设 startTime 存的是本地时间（数据库 DATETIME），我们将其视为系统默认时区的 LocalDateTime，
        // 然后转换到 UTC Instant。如果你的 start_time 已经是 UTC，请改用 ZoneOffset.UTC 直接转换。
        Instant expireInstant = startTime.atZone(ZoneId.systemDefault()).toInstant().plus(d);
        return DateTimeFormatter.ISO_INSTANT.format(expireInstant);
    }

    /**
     * 判断是否已经到期： startTime + duration < now
     */
    public static boolean isExpired(LocalDateTime startTime, String durationStr) {
        Duration d = parseSimpleDuration(durationStr);
        Instant expire = startTime.atZone(ZoneId.systemDefault()).toInstant().plus(d);
        Instant now = Instant.now();
        return expire.isBefore(now);
    }
}
