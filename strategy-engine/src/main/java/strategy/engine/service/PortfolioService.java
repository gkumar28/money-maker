package strategy.engine.service;

import strategy.engine.schemaobject.HoldingDto;
import strategy.engine.schemaobject.PortfolioDto;
import strategy.engine.schemaobject.TradeDto;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {
    void applyOrder(TradeDto tradeDto);

    BigDecimal getTotalValue();

    void init(BigDecimal totalCapital, BigDecimal availableCapital);

    void resetPortfolio(BigDecimal newCapital);

    HoldingDto getCurrentHoldings(String instrument);

    List<String> getInstruments();

    PortfolioDto getPortfolio();
}
