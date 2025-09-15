package broker.integrator.service;

import broker.integrator.schemaobject.Bar;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketDataService {

    List<Bar> getHistoricalData(String instrument, String exchange, LocalDateTime from, LocalDateTime to, String interval);

    void updateInstrumentMasterList();

    long getInstrumentToken(String tradingSymbol, String exchange);
}
