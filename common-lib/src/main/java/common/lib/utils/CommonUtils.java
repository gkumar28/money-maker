package common.lib.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

    public static Duration getDuration(String interval) {
        try {
            long value = Long.parseLong(interval.substring(0, interval.length() - 1));
            char unit = interval.charAt(interval.length() - 1);

            return switch (unit) {
                case 's' -> Duration.ofSeconds(value);
                case 'm' -> Duration.ofMinutes(value);
                case 'h' -> Duration.ofHours(value);
                case 'd' -> Duration.ofDays(value);
                default -> throw new IllegalArgumentException("Unknown unit: " + unit);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse given interval value: " + interval);
        }
    }
}
