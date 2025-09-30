package strategy.engine.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.Trade;
import strategy.engine.schemaobject.analysis.TradingRecord;
import strategy.engine.service.PortfolioManagementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Service
@Slf4j
public class PortfolioManagementServiceImpl implements PortfolioManagementService {


    @Override
    public BigDecimal getTotalValue(Portfolio portfolio) {
        return portfolio.getAvailableCapital();
    }

    @Override
    public void init(Portfolio portfolio, BigDecimal initialCapital, BigDecimal availableCapital) {
        portfolio.setInitialCapital(initialCapital);
        portfolio.setAvailableCapital(availableCapital);
        portfolio.getTpPrices().clear();
        portfolio.getSlPrices().clear();
        portfolio.setLastUpdated();
    }

    @Override
    public void resetPortfolio(Portfolio portfolio, BigDecimal newCapital) {
        portfolio.getHoldings().clear();
        portfolio.setAvailableCapital(newCapital);
        portfolio.setRealizedPnL(BigDecimal.ZERO);
        portfolio.setInitialCapital(newCapital);
        portfolio.setCurrentInvestedCapital(BigDecimal.ZERO);
        portfolio.setMaxInvestedCapital(BigDecimal.ZERO);
        portfolio.getTpPrices().clear();
        portfolio.getSlPrices().clear();
        portfolio.setLastUpdated();
    }

    @Override
    public Holding getCurrentHoldings(Portfolio portfolio, String instrument) {
        return portfolio.getHoldings().computeIfAbsent(instrument, Holding::new);
    }

    @Override
    public List<String> getInstruments() {
        return List.of("RELIANCE");
    }

    @Override
    public void updateLastTradedPrice(Portfolio portfolio, String instrument, BigDecimal currentPrice) {
        Holding holding = getCurrentHoldings(portfolio, instrument);
        if (holding.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        holding.setLastTradePrice(currentPrice);
    }

    @Override
    public void applyTrade(Portfolio portfolio, Trade trade, TradingRecord tradingRecord) {
        if (trade == null || trade.getQuantity().compareTo(BigDecimal.ZERO) == 0) return;

        switch (trade.getTradeType()) {
            case BUY -> processBuy(portfolio, trade, tradingRecord);
            case SELL -> processSell(portfolio, trade, tradingRecord);
        }

        Holding holding = portfolio.getHoldings().get(trade.getInstrument());
        log.debug("{}: invested capital: {} Avg Entry Price: {} Quantity: {}", holding.getInstrument(), sanitize(holding.getCurrentInvestedCapital()), sanitize(holding.getAvgEntryPrice()), sanitize(holding.getQuantity()));
        portfolio.setLastUpdated();
    }

    private void processBuy(Portfolio portfolio, Trade trade, TradingRecord tradingRecord) {
        String symbol = trade.getInstrument();

        Holding holding = portfolio.getHoldings().getOrDefault(symbol, new Holding(symbol));
        // trading record is called first to log trade so it always has latest information of open trades
        BigDecimal newInvestedCapital = calculateCurrentInvestedCapital(tradingRecord);
        BigDecimal tradeOriginalCost = newInvestedCapital.subtract(holding.getCurrentInvestedCapital());

        if (portfolio.getAvailableCapital().compareTo(tradeOriginalCost) < 0) {
            throw new IllegalStateException("BUY trade cannot exceed available capital");
        }

        BigDecimal newQty = holding.getQuantity().add(trade.getQuantity());

        holding.setQuantity(newQty);
        holding.setAvgEntryPrice(newInvestedCapital.divide(newQty, 2, RoundingMode.HALF_UP));
        // always update by difference of new capital and current capital -> original cost of trade
        updateInvestedCapital(portfolio, holding, tradeOriginalCost);

        portfolio.getHoldings().put(symbol, holding);
        portfolio.setAvailableCapital(portfolio.getAvailableCapital().subtract(tradeOriginalCost));
    }

    private BigDecimal calculateCurrentInvestedCapital(TradingRecord tradingRecord) {
        return tradingRecord.getOpenPosition().getInvestedCapital();
    }

    private void processSell(Portfolio portfolio, Trade trade, TradingRecord tradingRecord) {
        String symbol = trade.getInstrument();
        Holding holding = portfolio.getHoldings().getOrDefault(symbol, new Holding(symbol));
        // trading record is called first to log trade so it always has the latest information of open trades
        BigDecimal newInvestedCapital = calculateCurrentInvestedCapital(tradingRecord);
        BigDecimal tradeOriginalCost = newInvestedCapital.subtract(holding.getCurrentInvestedCapital()); // -ve of invested value

        if (holding.getQuantity().compareTo(trade.getQuantity()) < 0) return;

        BigDecimal sellQty = trade.getQuantity();
        BigDecimal newQty = holding.getQuantity().subtract(sellQty);
        BigDecimal value = trade.getPrice().multiply(sellQty);
        BigDecimal pnl = value.add(tradeOriginalCost);

        // Update holding
        holding.setQuantity(newQty);
        if (BigDecimal.ZERO.compareTo(newQty) == 0) {
            holding.setAvgEntryPrice(BigDecimal.ZERO);
        } else {
            holding.setAvgEntryPrice(newInvestedCapital.divide(newQty, 2, RoundingMode.HALF_UP));
        }
        updateInvestedCapital(portfolio, holding, tradeOriginalCost);
        portfolio.getHoldings().put(symbol, holding);
        portfolio.setAvailableCapital(portfolio.getAvailableCapital().add(value));
        portfolio.setRealizedPnL(portfolio.getRealizedPnL().add(pnl));
    }

    private void updateInvestedCapital(Portfolio portfolio, Holding holding, BigDecimal costOfOpenTrade) {
        holding.setCurrentInvestedCapital(holding.getCurrentInvestedCapital().add(costOfOpenTrade));
        if (holding.getCurrentInvestedCapital().compareTo(holding.getMaxInvestedCapital()) > 0) {
            holding.setMaxInvestedCapital(holding.getCurrentInvestedCapital());
        }
        portfolio.setCurrentInvestedCapital(portfolio.getCurrentInvestedCapital().add(costOfOpenTrade));
        if (portfolio.getCurrentInvestedCapital().compareTo(portfolio.getMaxInvestedCapital()) > 0) {
            portfolio.setMaxInvestedCapital(portfolio.getCurrentInvestedCapital());
        }
    }
}
