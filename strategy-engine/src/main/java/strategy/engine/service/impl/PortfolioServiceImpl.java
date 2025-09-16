package strategy.engine.service.impl;

import org.springframework.stereotype.Service;
import strategy.engine.schemaobject.HoldingDto;
import strategy.engine.schemaobject.PortfolioDto;
import strategy.engine.schemaobject.TradeDto;
import strategy.engine.service.PortfolioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
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
        portfolioDto.getTradeDtoList().clear();
        portfolioDto.setAvailableCapital(newCapital);
        portfolioDto.setRealizedPnL(BigDecimal.ZERO);
        portfolioDto.setTotalCapital(newCapital);
        portfolioDto.setCurrentInvestedCapital(BigDecimal.ZERO);
        portfolioDto.setMaxInvestedCapital(BigDecimal.ZERO);
        portfolioDto.setLastUpdated();
    }

    @Override
    public void applyOrder(TradeDto tradeDto) {
        if (tradeDto == null || tradeDto.getQuantity() == 0) return;

        switch (tradeDto.getDirection()) {
            case BUY -> processBuy(tradeDto);
            case SELL -> processSell(tradeDto);
        }

        portfolioDto.setLastUpdated();
    }

    @Override
    public HoldingDto getCurrentHoldings(String instrument) {
        return portfolioDto.getHoldings().get(instrument);
    }

    @Override
    public List<String> getInstruments() {
        return List.of("RELIANCE");
    }

    @Override
    public PortfolioDto getPortfolio() {
        return portfolioDto;
    }

    private void processBuy(TradeDto tradeDto) {
        String symbol = tradeDto.getInstrument();
        BigDecimal cost = tradeDto.getPrice().multiply(BigDecimal.valueOf(tradeDto.getQuantity()));

        if (portfolioDto.getAvailableCapital().compareTo(cost) < 0) {
            throw new IllegalStateException("BUY trade cannot exceed available capital");
        }

        HoldingDto holding = portfolioDto.getHoldings().getOrDefault(symbol, new HoldingDto());
        holding.setInstrument(symbol);

        // Calculate new average price
        int existingQty = holding.getQuantity();
        BigDecimal existingCost = holding.getAvgEntryPrice() != null
            ? holding.getAvgEntryPrice().multiply(BigDecimal.valueOf(existingQty))
            : BigDecimal.ZERO;

        BigDecimal newTotalCost = existingCost.add(cost);
        int newQty = existingQty + tradeDto.getQuantity();

        holding.setQuantity(newQty);
        holding.setAvgEntryPrice(newTotalCost.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP));

        updateInvestedCapital(holding, cost);
        portfolioDto.getHoldings().put(symbol, holding);
        portfolioDto.setAvailableCapital(portfolioDto.getAvailableCapital().subtract(cost));

        portfolioDto.getTradeDtoList().add(tradeDto);
    }

    private void processSell(TradeDto tradeDto) {
        String symbol = tradeDto.getInstrument();
        HoldingDto holding = portfolioDto.getHoldings().get(symbol);
        if (holding == null || holding.getQuantity() < tradeDto.getQuantity()) return;

        int sellQty = tradeDto.getQuantity();
        BigDecimal entryPrice = holding.getAvgEntryPrice();
        BigDecimal value = tradeDto.getPrice().multiply(BigDecimal.valueOf(sellQty));
        BigDecimal buyCost = entryPrice.multiply(BigDecimal.valueOf(sellQty));
        BigDecimal pnl = value.subtract(buyCost);

        // Update holding
        holding.setQuantity(holding.getQuantity() - sellQty);
        if (holding.getQuantity() == 0) {
            portfolioDto.getHoldings().remove(symbol);
        } else {
            portfolioDto.getHoldings().put(symbol, holding);
        }

        // Update capital and PnL
        updateInvestedCapital(holding, value.multiply(BigDecimal.valueOf(-1)));
        portfolioDto.setAvailableCapital(portfolioDto.getAvailableCapital().add(value));
        portfolioDto.setRealizedPnL(portfolioDto.getRealizedPnL().add(pnl));
        portfolioDto.getTradeDtoList().add(tradeDto);
    }

    private void updateInvestedCapital(HoldingDto holding, BigDecimal cost) {
        holding.setCurrentInvestedCapital(holding.getCurrentInvestedCapital().add(cost));
        if (holding.getCurrentInvestedCapital().compareTo(holding.getMaxInvestedCapital()) > 0) {
            holding.setMaxInvestedCapital(holding.getCurrentInvestedCapital());
        }
        portfolioDto.setCurrentInvestedCapital(portfolioDto.getCurrentInvestedCapital().add(cost));
        if (portfolioDto.getCurrentInvestedCapital().compareTo(portfolioDto.getMaxInvestedCapital()) > 0) {
            portfolioDto.setMaxInvestedCapital(portfolioDto.getCurrentInvestedCapital());
        }
    }
}
