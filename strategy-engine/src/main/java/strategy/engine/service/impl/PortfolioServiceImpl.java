package strategy.engine.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.Trade;
import strategy.engine.schemaobject.analysis.MultiPositionTradingRecord;
import strategy.engine.service.PortfolioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Service
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final Portfolio portfolio = new Portfolio();

    @Override
    public BigDecimal getTotalValue() {
        return portfolio.getAvailableCapital();
    }

    @Override
    public void init(BigDecimal initialCapital, BigDecimal availableCapital) {
        portfolio.setInitialCapital(initialCapital);
        portfolio.setAvailableCapital(availableCapital);
        portfolio.setLastUpdated();
    }

    @Override
    public void resetPortfolio(BigDecimal newCapital) {
        portfolio.getHoldings().clear();
        portfolio.setAvailableCapital(newCapital);
        portfolio.setRealizedPnL(BigDecimal.ZERO);
        portfolio.setInitialCapital(newCapital);
        portfolio.setCurrentInvestedCapital(BigDecimal.ZERO);
        portfolio.setMaxInvestedCapital(BigDecimal.ZERO);
        portfolio.setLastUpdated();
    }

    @Override
    public void applyTrade(Trade trade, MultiPositionTradingRecord tradingRecord) {
        if (trade == null || trade.getQuantity() == 0) return;

        switch (trade.getDirection()) {
            case BUY -> processBuy(trade, tradingRecord);
            case SELL -> processSell(trade, tradingRecord);
        }

        Holding holding = portfolio.getHoldings().get(trade.getInstrument());
        log.debug("{}: invested capital: {} Avg Entry Price: {} Quantity: {}", holding.getInstrument(), sanitize(holding.getCurrentInvestedCapital()), sanitize(holding.getAvgEntryPrice()), holding.getQuantity());
        portfolio.setLastUpdated();
    }

    @Override
    public Holding getCurrentHoldings(String instrument) {
        return portfolio.getHoldings().computeIfAbsent(instrument, Holding::new);
    }

    @Override
    public List<String> getInstruments() {
        return List.of("RELIANCE");
    }

    @Override
    public Portfolio getPortfolio() {
        return portfolio;
    }

    @Override
    public void updateLastTradedPrice(String instrument, BigDecimal currentPrice) {
        Holding holding = getCurrentHoldings(instrument);
        if (holding.getQuantity() == 0) {
            return;
        }

        holding.setLastTradePrice(currentPrice);
    }

    private void processBuy(Trade trade, MultiPositionTradingRecord tradingRecord) {
        String symbol = trade.getInstrument();

        Holding holding = portfolio.getHoldings().getOrDefault(symbol, new Holding(symbol));
        // trading record is called first to log trade so it always has latest information of open trades
        BigDecimal newInvestedCapital = calculateCurrentInvestedCapital(tradingRecord);
        BigDecimal tradeOriginalCost = newInvestedCapital.subtract(holding.getCurrentInvestedCapital());

        if (portfolio.getAvailableCapital().compareTo(tradeOriginalCost) < 0) {
            throw new IllegalStateException("BUY trade cannot exceed available capital");
        }

        int newQty = holding.getQuantity() + trade.getQuantity();

        holding.setQuantity(newQty);
        holding.setAvgEntryPrice(newInvestedCapital.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP));
        // always update by difference of new capital and current capital -> original cost of trade
        updateInvestedCapital(holding, tradeOriginalCost);

        portfolio.getHoldings().put(symbol, holding);
        portfolio.setAvailableCapital(portfolio.getAvailableCapital().subtract(tradeOriginalCost));
    }

    private BigDecimal calculateCurrentInvestedCapital(MultiPositionTradingRecord tradingRecord) {
        return tradingRecord.getUnRealizedCapitalInPartialPosition().bigDecimalValue();
    }

    private void processSell(Trade trade, MultiPositionTradingRecord tradingRecord) {
        String symbol = trade.getInstrument();
        Holding holding = portfolio.getHoldings().getOrDefault(symbol, new Holding(symbol));
        // trading record is called first to log trade so it always has latest information of open trades
        BigDecimal newInvestedCapital = calculateCurrentInvestedCapital(tradingRecord);
        BigDecimal tradeOriginalCost = newInvestedCapital.subtract(holding.getCurrentInvestedCapital()); // -ve of invested value

        if (holding.getQuantity() < trade.getQuantity()) return;

        int sellQty = trade.getQuantity();
        int newQty = holding.getQuantity() - sellQty;
        BigDecimal value = trade.getPrice().multiply(BigDecimal.valueOf(sellQty));
        BigDecimal pnl = value.add(tradeOriginalCost);

        // Update holding
        holding.setQuantity(newQty);
        if (0 == newQty) {
            holding.setAvgEntryPrice(BigDecimal.ZERO);
        } else {
            holding.setAvgEntryPrice(newInvestedCapital.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP));
        }
        updateInvestedCapital(holding, tradeOriginalCost);
        portfolio.getHoldings().put(symbol, holding);
        portfolio.setAvailableCapital(portfolio.getAvailableCapital().add(value));
        portfolio.setRealizedPnL(portfolio.getRealizedPnL().add(pnl));
    }

    private void updateInvestedCapital(Holding holding, BigDecimal costOfOpenTrade) {
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
