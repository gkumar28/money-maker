package strategy.engine.schemaobject;

import lombok.Data;
import org.ta4j.core.Position;
import strategy.engine.schemaobject.analysis.ExtendedTradingRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TradingReportGenerator {
    private final PortfolioDto portfolioDto;
    private final Map<String, ExtendedTradingRecord> tradingRecords = new HashMap<>();

    public TradingReportGenerator(PortfolioDto portfolioDto) {
        this.portfolioDto = portfolioDto;
    }

    public void setTradingRecord(String instrument, ExtendedTradingRecord tradingRecord) {
        tradingRecords.put(instrument, tradingRecord);
    }

    public TradingReport generate() {
        TradingReport portfolioReport = new TradingReport();

        BigDecimal totalCapital = portfolioDto.getTotalCapital() != null ? portfolioDto.getTotalCapital() : BigDecimal.ZERO;
        portfolioReport.setTotalCapital(totalCapital);

        List<TradingReport> subReports = new ArrayList<>();

        BigDecimal portfolioTotalInvestedCapital = BigDecimal.ZERO;
        BigDecimal portfolioRealizedPnL = BigDecimal.ZERO;
        int portfolioProfitCount = 0;
        int portfolioBreakEvenCount = 0;
        int portfolioLossCount = 0;
        BigDecimal portfolioProfitLoss = BigDecimal.ZERO;
        BigDecimal portfolioProfit = BigDecimal.ZERO;
        BigDecimal portfolioLoss = BigDecimal.ZERO;

        for (String instrument: tradingRecords.keySet()) {
            TradingReport instrumentReport = generate(instrument);
            subReports.add(instrumentReport);

            portfolioTotalInvestedCapital = portfolioTotalInvestedCapital.add(instrumentReport.getTotalInvestedCapital());
            portfolioRealizedPnL = portfolioRealizedPnL.add(instrumentReport.getRealizedPnL());
            portfolioProfitCount += instrumentReport.getProfitCount();
            portfolioBreakEvenCount += instrumentReport.getBreakEvenCount();
            portfolioLossCount += instrumentReport.getLossCount();
            portfolioProfitLoss = portfolioProfitLoss.add(instrumentReport.getProfitLoss());
            portfolioProfit = portfolioProfit.add(instrumentReport.getProfit());
            portfolioLoss = portfolioLoss.add(instrumentReport.getLoss());
        }

        // Portfolio level profit loss %
        BigDecimal portfolioProfitLossPercentage = BigDecimal.ZERO;
        if (portfolioTotalInvestedCapital.compareTo(BigDecimal.ZERO) > 0) {
            portfolioProfitLossPercentage = portfolioProfitLoss.divide(portfolioTotalInvestedCapital, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        portfolioReport.setTotalCapital(portfolioDto.getTotalCapital());
        portfolioReport.setTotalInvestedCapital(portfolioTotalInvestedCapital);
        portfolioReport.setCurrentInvestedCapital(portfolioDto.getCurrentInvestedCapital());
        portfolioReport.setMaxInvestedCapital(portfolioDto.getMaxInvestedCapital());
        portfolioReport.setRealizedPnL(portfolioRealizedPnL);

        portfolioReport.setProfitCount(portfolioProfitCount);
        portfolioReport.setBreakEvenCount(portfolioBreakEvenCount);
        portfolioReport.setLossCount(portfolioLossCount);

        portfolioReport.setProfitLoss(portfolioProfitLoss);
        portfolioReport.setProfitLossPercentage(portfolioProfitLossPercentage);
        portfolioReport.setProfit(portfolioProfit);
        portfolioReport.setLoss(portfolioLoss);

        portfolioReport.setSubReports(subReports);

        return portfolioReport;
    }

    private TradingReport generate(String instrument) {
        ExtendedTradingRecord record = tradingRecords.get(instrument);
        HoldingDto holding = portfolioDto.getHoldings().getOrDefault(instrument, new HoldingDto());

        TradingReport instrumentReport = new TradingReport();
        instrumentReport.setInstrument(instrument);

        BigDecimal unrealizedInvestedCapital = record.getCurrentInvestedCapital().bigDecimalValue();
        BigDecimal realizedInvestedCapital = BigDecimal.ZERO;
        BigDecimal realizedPnL = BigDecimal.ZERO;

        int profitCount = 0;
        int breakEvenCount = 0;
        int lossCount = 0;

        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        // Iterate over all closed positions
        for (Position position : record.getPositions()) {
            BigDecimal entryCost = position.getEntry().getPricePerAsset().multipliedBy(position.getEntry().getAmount()).bigDecimalValue();
            BigDecimal exitCost = position.getExit().getPricePerAsset().multipliedBy(position.getExit().getAmount()).bigDecimalValue();

            BigDecimal positionRealizedPnL = exitCost.subtract(entryCost);
            realizedPnL = realizedPnL.add(positionRealizedPnL);
            totalProfitLoss = totalProfitLoss.add(positionRealizedPnL);
            realizedInvestedCapital = realizedInvestedCapital.add(entryCost);

            // Count profit/break-even/loss trades
            int cmp = positionRealizedPnL.compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                profitCount++;
                totalProfit = totalProfit.add(positionRealizedPnL);
            } else if (cmp == 0) {
                breakEvenCount++;
            } else {
                lossCount++;
                totalLoss = totalLoss.add(positionRealizedPnL);
            }
        }

        // add realized part of partial position if any
        realizedInvestedCapital = realizedInvestedCapital.add(record.getRealizedCapitalFromPartialPosition().bigDecimalValue());
        BigDecimal profitLossFromRealizedPartialPosition = record.getRealizedProfitLossFromPartialPosition().bigDecimalValue();
        totalProfitLoss = totalProfitLoss.add(profitLossFromRealizedPartialPosition);
        realizedPnL = realizedPnL.add(profitLossFromRealizedPartialPosition);
        if (profitLossFromRealizedPartialPosition.compareTo(BigDecimal.ZERO) > 0) {
            totalProfit = totalProfit.add(profitLossFromRealizedPartialPosition);
        } else {
            totalLoss = totalLoss.add(profitLossFromRealizedPartialPosition);
        }

        // Calculate total profit/loss percentage (avoid div by zero)
        BigDecimal totalProfitLossPercentage = BigDecimal.ZERO;
        if (realizedInvestedCapital.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercentage = totalProfitLoss.divide(realizedInvestedCapital, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        instrumentReport.setInstrument(instrument);
        instrumentReport.setCurrentInvestedCapital(holding.getCurrentInvestedCapital());
        instrumentReport.setMaxInvestedCapital(holding.getMaxInvestedCapital());
        instrumentReport.setTotalInvestedCapital(realizedInvestedCapital.add(unrealizedInvestedCapital));
        instrumentReport.setRealizedPnL(realizedPnL);

        instrumentReport.setProfitCount(profitCount);
        instrumentReport.setBreakEvenCount(breakEvenCount);
        instrumentReport.setLossCount(lossCount);

        instrumentReport.setProfitLoss(totalProfitLoss);
        instrumentReport.setProfitLossPercentage(totalProfitLossPercentage);
        instrumentReport.setProfit(totalProfit);
        instrumentReport.setLoss(totalLoss);

        return instrumentReport;
    }

}
