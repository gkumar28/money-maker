package strategy.engine.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import strategy.engine.schemaobject.HoldingDto;
import strategy.engine.schemaobject.PortfolioDto;
import strategy.engine.schemaobject.TradeDto;
import strategy.engine.schemaobject.analysis.MultiPositionTradingRecord;
import strategy.engine.service.PortfolioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Service
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioDto portfolioDto = new PortfolioDto();

    @Override
    public BigDecimal getTotalValue() {
        return portfolioDto.getAvailableCapital().add(portfolioDto.getRealizedPnL());
    }

    @Override
    public void init(BigDecimal totalCapital, BigDecimal availableCapital) {
        portfolioDto.setTotalCapital(totalCapital);
        portfolioDto.setAvailableCapital(availableCapital);
        portfolioDto.setLastUpdated();
    }

    @Override
    public void resetPortfolio(BigDecimal newCapital) {
        portfolioDto.getHoldings().clear();
        portfolioDto.setAvailableCapital(newCapital);
        portfolioDto.setRealizedPnL(BigDecimal.ZERO);
        portfolioDto.setTotalCapital(newCapital);
        portfolioDto.setCurrentInvestedCapital(BigDecimal.ZERO);
        portfolioDto.setMaxInvestedCapital(BigDecimal.ZERO);
        portfolioDto.setLastUpdated();
    }

    @Override
    public void applyTrade(TradeDto tradeDto, MultiPositionTradingRecord tradingRecord) {
        if (tradeDto == null || tradeDto.getQuantity() == 0) return;

        switch (tradeDto.getDirection()) {
            case BUY -> processBuy(tradeDto, tradingRecord);
            case SELL -> processSell(tradeDto, tradingRecord);
        }

        HoldingDto holding = portfolioDto.getHoldings().get(tradeDto.getInstrument());
        log.debug("{}: invested capital: {} Avg Entry Price: {} Quantity: {}", holding.getInstrument(), sanitize(holding.getCurrentInvestedCapital()), sanitize(holding.getAvgEntryPrice()), holding.getQuantity());
        portfolioDto.setLastUpdated();
    }

    @Override
    public HoldingDto getCurrentHoldings(String instrument) {
        return portfolioDto.getHoldings().computeIfAbsent(instrument, HoldingDto::new);
    }

    @Override
    public List<String> getInstruments() {
        return List.of("RELIANCE");
    }

    @Override
    public PortfolioDto getPortfolio() {
        return portfolioDto;
    }

    private void processBuy(TradeDto tradeDto, MultiPositionTradingRecord tradingRecord) {
        String symbol = tradeDto.getInstrument();

        HoldingDto holding = portfolioDto.getHoldings().getOrDefault(symbol, new HoldingDto(symbol));
        // trading record is called first to log trade so it always has latest information of open trades
        BigDecimal newInvestedCapital = calculateCurrentInvestedCapital(tradingRecord);
        BigDecimal tradeOriginalCost = newInvestedCapital.subtract(holding.getCurrentInvestedCapital());

        if (portfolioDto.getAvailableCapital().compareTo(tradeOriginalCost) < 0) {
            throw new IllegalStateException("BUY trade cannot exceed available capital");
        }

        int newQty = holding.getQuantity() + tradeDto.getQuantity();

        holding.setQuantity(newQty);
        holding.setAvgEntryPrice(newInvestedCapital.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP));
        // always update by difference of new capital and current capital -> original cost of trade
        updateInvestedCapital(holding, tradeOriginalCost);

        portfolioDto.getHoldings().put(symbol, holding);
        portfolioDto.setAvailableCapital(portfolioDto.getAvailableCapital().subtract(tradeOriginalCost));
    }

    private BigDecimal calculateCurrentInvestedCapital(MultiPositionTradingRecord tradingRecord) {
        return tradingRecord.getCurrentInvestedCapital().bigDecimalValue();
    }

    private void processSell(TradeDto tradeDto, MultiPositionTradingRecord tradingRecord) {
        String symbol = tradeDto.getInstrument();
        HoldingDto holding = portfolioDto.getHoldings().getOrDefault(symbol, new HoldingDto(symbol));
        // trading record is called first to log trade so it always has latest information of open trades
        BigDecimal newInvestedCapital = calculateCurrentInvestedCapital(tradingRecord);
        BigDecimal tradeOriginalCost = newInvestedCapital.subtract(holding.getCurrentInvestedCapital()); // -ve of invested value

        if (holding.getQuantity() < tradeDto.getQuantity()) return;

        int sellQty = tradeDto.getQuantity();
        int newQty = holding.getQuantity() - sellQty;
        BigDecimal value = tradeDto.getPrice().multiply(BigDecimal.valueOf(sellQty));
        BigDecimal pnl = value.add(tradeOriginalCost);

        // Update holding
        holding.setQuantity(newQty);
        if (0 == newQty) {
            holding.setAvgEntryPrice(BigDecimal.ZERO);
        } else {
            holding.setAvgEntryPrice(newInvestedCapital.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP));
        }
        updateInvestedCapital(holding, tradeOriginalCost);
        portfolioDto.getHoldings().put(symbol, holding);
        portfolioDto.setAvailableCapital(portfolioDto.getAvailableCapital().add(value));
        portfolioDto.setRealizedPnL(portfolioDto.getRealizedPnL().add(pnl));
    }

    private void updateInvestedCapital(HoldingDto holding, BigDecimal costOfOpenTrade) {
        holding.setCurrentInvestedCapital(holding.getCurrentInvestedCapital().add(costOfOpenTrade));
        if (holding.getCurrentInvestedCapital().compareTo(holding.getMaxInvestedCapital()) > 0) {
            holding.setMaxInvestedCapital(holding.getCurrentInvestedCapital());
        }
        portfolioDto.setCurrentInvestedCapital(portfolioDto.getCurrentInvestedCapital().add(costOfOpenTrade));
        if (portfolioDto.getCurrentInvestedCapital().compareTo(portfolioDto.getMaxInvestedCapital()) > 0) {
            portfolioDto.setMaxInvestedCapital(portfolioDto.getCurrentInvestedCapital());
        }
    }
}
