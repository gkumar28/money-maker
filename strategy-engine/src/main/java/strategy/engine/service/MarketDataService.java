package strategy.engine.service;

import org.ta4j.core.Bar;
import org.ta4j.core.num.NumFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public interface MarketDataService {

    Path loadRawData(String instrument, String exchange, LocalDateTime fromDate, LocalDateTime toDate, String interval);

}
