package strategy.engine.service;

import org.ta4j.core.BarSeries;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MarketDataService {

    BarSeries loadHistoricalData(String instrument, LocalDate fromDate, LocalDate toDate);

    BarSeries loadHistoricalData(String instrument, String fromDate, String toDate);

    List<Map<String, Object>> loadRawData(String instrument, LocalDate fromDate, LocalDate toDate);

    List<Map<String, Object>> loadRawData(String instrument, String fromDate, String toDate);
}
