package strategy.engine.service;

import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.Map;

public interface MarketDataService {

    BarSeries loadHistoricalData(String instrument, String fromDate, String toDate);

    List<Map<String, Object>> loadRawData(String instrument, String fromDate, String toDate);
}
