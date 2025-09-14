package broker.integrator.service;

import broker.integrator.schemaobject.Bar;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketDataService {

    List<Bar> getHistoricalData(String instrument, LocalDateTime from, LocalDateTime to, String interval);
}
