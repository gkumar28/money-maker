package strategy.engine.service;

import strategy.engine.schemaobject.analysis.TradingRecord;

import java.time.LocalDateTime;

public interface TradingRecordManagementService {

    void writeToFile(TradingRecord tradingRecord, String instrument, String exchange, LocalDateTime fromDate, LocalDateTime toDate, String interval);
}
