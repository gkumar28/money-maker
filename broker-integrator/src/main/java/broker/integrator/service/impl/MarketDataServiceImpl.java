package broker.integrator.service.impl;

import broker.integrator.component.ZerodhaClient;
import broker.integrator.schemaobject.Bar;
import broker.integrator.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataServiceImpl implements MarketDataService {

    private final ZerodhaClient zerodhaClient;

    @Override
    public List<Bar> getHistoricalData(String instrument, LocalDateTime from, LocalDateTime to, String interval) {
        //return zerodhaClient.getHistoricalData(instrument, from, to, interval, true, true);

        Bar bar = new Bar();
        bar.setTimeStamp("timestamp");
        return List.of(bar);

    }

    private long estimatePoints(LocalDateTime from, LocalDateTime to, Duration interval) {
        Duration totalDuration = Duration.between(from, to);
        long points = totalDuration.toMillis() / interval.toMillis();
        return points + 1;
    }

    private Duration getIntervalDuration(String interval) {
        return switch (interval.toLowerCase().trim()) {
            case "minute", "1minute" -> Duration.ofMinutes(1);
            case "5minute" -> Duration.ofMinutes(5);
            case "60minute", "1hour" -> Duration.ofHours(1);
            case "1day", "day" -> Duration.ofDays(1);
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval);
        };
    }
}
