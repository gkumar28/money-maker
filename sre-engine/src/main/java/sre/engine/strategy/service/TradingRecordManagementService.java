package sre.engine.strategy.service;

import org.ta4j.core.TradingRecord;

import java.time.LocalDateTime;

public interface TradingRecordManagementService {

    void writeToFile(TradingRecord tradingRecord, String instrument, String exchange, LocalDateTime fromDate, LocalDateTime toDate, String interval);
}
