package strategy.engine.service;

import strategy.engine.schemaobject.analysis.TradingRecord;

public interface TradingRecordManagementService {
    void writeToFile(TradingRecord tradingRecord);
}
