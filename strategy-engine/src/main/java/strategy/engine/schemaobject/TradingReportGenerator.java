package strategy.engine.schemaobject;

import lombok.Data;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TradingReportGenerator {
    private final Portfolio portfolio;
    private final Map<String, TradingRecord> tradingRecords = new HashMap<>();

    public TradingReportGenerator(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public void setTradingRecord(String instrument, TradingRecord tradingRecord) {
        tradingRecords.put(instrument, tradingRecord);
    }

    public TradingReport generate() {
        TradingReport portfolioReport = new TradingReport();

        List<TradingReport> subReports = new ArrayList<>();

        BigDecimal portfolioTotalInvestedCapital = BigDecimal.ZERO;
        BigDecimal portfolioUnrealizedPnL = BigDecimal.ZERO;
        int portfolioProfitCount = 0;
        int portfolioBreakEvenCount = 0;
        int portfolioLossCount = 0;
        BigDecimal portfolioProfitLoss = BigDecimal.ZERO;
        BigDecimal portfolioProfit = BigDecimal.ZERO;
        BigDecimal portfolioLoss = BigDecimal.ZERO;
        int portfolioEntryTradeCount = 0;
        int portfolioExitTradeCount = 0;

        for (String instrument: tradingRecords.keySet()) {
            TradingReport instrumentReport = generate(instrument);
            subReports.add(instrumentReport);

            portfolioTotalInvestedCapital = portfolioTotalInvestedCapital.add(instrumentReport.getTotalInvestedCapital());
            portfolioUnrealizedPnL = portfolioUnrealizedPnL.add(instrumentReport.getUnrealizedPnL());
            portfolioProfitCount += instrumentReport.getProfitCount();
            portfolioBreakEvenCount += instrumentReport.getBreakEvenCount();
            portfolioLossCount += instrumentReport.getLossCount();
            portfolioProfitLoss = portfolioProfitLoss.add(instrumentReport.getProfitLoss());
            portfolioProfit = portfolioProfit.add(instrumentReport.getProfit());
            portfolioLoss = portfolioLoss.add(instrumentReport.getLoss());
            portfolioEntryTradeCount += instrumentReport.getEntryTradeCount();
            portfolioExitTradeCount += instrumentReport.getExitTradeCount();
        }

        // Portfolio level profit loss %
        BigDecimal portfolioProfitLossPercentage = BigDecimal.ZERO;
        if (portfolioTotalInvestedCapital.compareTo(BigDecimal.ZERO) > 0) {
            portfolioProfitLossPercentage = portfolioProfitLoss.divide(portfolioTotalInvestedCapital, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        portfolioReport.setInitialCapital(portfolio.getInitialCapital());
        portfolioReport.setAvailableCapital(portfolio.getAvailableCapital());
        portfolioReport.setTotalInvestedCapital(portfolioTotalInvestedCapital);
        portfolioReport.setCurrentInvestedCapital(portfolio.getCurrentInvestedCapital());
        portfolioReport.setMaxInvestedCapital(portfolio.getMaxInvestedCapital());

        portfolioReport.setProfitCount(portfolioProfitCount);
        portfolioReport.setBreakEvenCount(portfolioBreakEvenCount);
        portfolioReport.setLossCount(portfolioLossCount);

        portfolioReport.setProfitLoss(portfolioProfitLoss);
        portfolioReport.setProfitLossPercentage(portfolioProfitLossPercentage);
        portfolioReport.setProfit(portfolioProfit);
        portfolioReport.setLoss(portfolioLoss);
        portfolioReport.setUnrealizedPnL(portfolioUnrealizedPnL);

        portfolioReport.setEntryTradeCount(portfolioEntryTradeCount);
        portfolioReport.setExitTradeCount(portfolioExitTradeCount);

        portfolioReport.setSubReports(subReports);

        return portfolioReport;
    }

    private TradingReport generate(String instrument) {
        TradingRecord tradingRecord = tradingRecords.get(instrument);
        Holding holding = portfolio.getHoldings().getOrDefault(instrument, new Holding());

        TradingReport instrumentReport = new TradingReport();
        instrumentReport.setInstrument(instrument);

        BigDecimal profitLoss = tradingRecord.getCurrentPosition().getProfit().bigDecimalValue();
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal loss = BigDecimal.ZERO;
        if (profitLoss.compareTo(BigDecimal.ZERO) > 0) {
            profit = profitLoss.add(profitLoss);
        } else {
            loss = loss.add(profitLoss);
        }
        BigDecimal investedCapital = tradingRecord.getCurrentPosition().getEntry().getValue().bigDecimalValue();
        BigDecimal unrealizedProfitLoss = holding.getLastTradePrice().multiply(holding.getQuantity()).subtract(investedCapital);

        int profitCount = 0;
        int breakEvenCount = 0;
        int lossCount = 0;
        int entryTradeFillCount = 0;
        int exitTradeFillCount = 0;

        // Iterate over all closed positions
        for (Position position : tradingRecord.getPositions()) {
            BigDecimal entryGrossValue = BigDecimal.ZERO;
            BigDecimal exitGrossValue = BigDecimal.ZERO;
            BigDecimal entryCost = BigDecimal.ZERO;
            BigDecimal exitCost = BigDecimal.ZERO;

            entryGrossValue = entryGrossValue.add(position.getEntry().getValue().bigDecimalValue());
            entryCost = entryCost.add(position.getEntry().getCost().bigDecimalValue());

            exitGrossValue = exitGrossValue.add(position.getExit().getValue().bigDecimalValue());
            exitCost = exitCost.add(position.getExit().getCost().bigDecimalValue());

            BigDecimal positionPnL = exitGrossValue.subtract(entryGrossValue).subtract(entryCost).subtract(exitCost);
            profitLoss = profitLoss.add(positionPnL);
            investedCapital = investedCapital.add(entryCost).add(entryGrossValue);

            entryTradeFillCount += position.getEntry().getFills().size();
            entryTradeFillCount += position.getExit().getFills().size();

            // Count profit/break-even/loss trades
            int cmp = positionPnL.compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                profitCount++;
                profit = profit.add(positionPnL);
            } else if (cmp == 0) {
                breakEvenCount++;
            } else {
                lossCount++;
                loss = loss.add(positionPnL);
            }
        }

        // Calculate total profit/loss percentage (avoid div by zero)
        BigDecimal profitLossPercentage = BigDecimal.ZERO;
        if (investedCapital.compareTo(BigDecimal.ZERO) > 0) {
            profitLossPercentage = profitLoss.divide(investedCapital, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        instrumentReport.setInstrument(instrument);
        instrumentReport.setCurrentInvestedCapital(holding.getCurrentInvestedCapital());
        instrumentReport.setMaxInvestedCapital(holding.getMaxInvestedCapital());
        instrumentReport.setTotalInvestedCapital(investedCapital);

        instrumentReport.setProfitCount(profitCount);
        instrumentReport.setBreakEvenCount(breakEvenCount);
        instrumentReport.setLossCount(lossCount);

        instrumentReport.setProfitLoss(profitLoss);
        instrumentReport.setProfitLossPercentage(profitLossPercentage);
        instrumentReport.setProfit(profit);
        instrumentReport.setLoss(loss);
        instrumentReport.setUnrealizedPnL(unrealizedProfitLoss);

        instrumentReport.setEntryTradeCount(entryTradeFillCount);
        instrumentReport.setExitTradeCount(exitTradeFillCount);

        return instrumentReport;
    }

}
