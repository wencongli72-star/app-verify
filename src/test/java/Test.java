import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class Test {
    public static void main(String[] args) {
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
