package strategy.engine.schemaobject;

import lombok.Data;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TradingReportGenerator {
    private final PortfolioDto portfolioDto;
    private final Map<String, TradingRecord> tradingRecords = new HashMap<>();

    public TradingReportGenerator(PortfolioDto portfolioDto) {
        this.portfolioDto = portfolioDto;
    }

    public void setTradingRecord(String instrument, TradingRecord tradingRecord) {
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
        BigDecimal portfolioTotalProfitLoss = BigDecimal.ZERO;
        BigDecimal portfolioTotalProfit = BigDecimal.ZERO;
        BigDecimal portfolioTotalLoss = BigDecimal.ZERO;

        for (String instrument: tradingRecords.keySet()) {
            TradingReport instrumentReport = generate(instrument);
            subReports.add(instrumentReport);

            portfolioTotalInvestedCapital = portfolioTotalInvestedCapital.add(instrumentReport.getTotalInvestedCapital());
            portfolioRealizedPnL = portfolioRealizedPnL.add(instrumentReport.getRealizedPnL());
            portfolioProfitCount += instrumentReport.getProfitCount();
            portfolioBreakEvenCount += instrumentReport.getBreakEvenCount();
            portfolioLossCount += instrumentReport.getLossCount();
            portfolioTotalProfitLoss = portfolioTotalProfitLoss.add(instrumentReport.getTotalProfitLoss());
            portfolioTotalProfit = portfolioTotalProfit.add(instrumentReport.getTotalProfit());
            portfolioTotalLoss = portfolioTotalLoss.add(instrumentReport.getTotalLoss());
        }

        // Portfolio level profit loss %
        BigDecimal portfolioTotalProfitLossPercentage = BigDecimal.ZERO;
        if (portfolioTotalInvestedCapital.compareTo(BigDecimal.ZERO) > 0) {
            portfolioTotalProfitLossPercentage = portfolioTotalProfitLoss.divide(portfolioTotalInvestedCapital, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        portfolioReport.setTotalCapital(portfolioDto.getTotalCapital());
        portfolioReport.setTotalInvestedCapital(portfolioTotalInvestedCapital);
        portfolioReport.setCurrentInvestedCapital(portfolioDto.getCurrentInvestedCapital());
        portfolioReport.setMaxInvestedCapital(portfolioDto.getMaxInvestedCapital());
        portfolioReport.setRealizedPnL(portfolioRealizedPnL);

        portfolioReport.setProfitCount(portfolioProfitCount);
        portfolioReport.setBreakEvenCount(portfolioBreakEvenCount);
        portfolioReport.setLossCount(portfolioLossCount);

        portfolioReport.setTotalProfitLoss(portfolioTotalProfitLoss);
        portfolioReport.setTotalProfitLossPercentage(portfolioTotalProfitLossPercentage);
        portfolioReport.setTotalProfit(portfolioTotalProfit);
        portfolioReport.setTotalLoss(portfolioTotalLoss);

        portfolioReport.setSubReports(subReports);

        return portfolioReport;
    }

    private TradingReport generate(String instrument) {
        TradingRecord record = tradingRecords.get(instrument);
        HoldingDto holding = portfolioDto.getHoldings().getOrDefault(instrument, new HoldingDto());

        TradingReport instrumentReport = new TradingReport();
        instrumentReport.setInstrument(instrument);

        BigDecimal totalInvestedCapital = BigDecimal.ZERO;
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
            BigDecimal exitCost = position.getExit().getNetPrice().multipliedBy(position.getExit().getAmount()).bigDecimalValue();

            BigDecimal positionRealizedPnL = exitCost.subtract(entryCost);
            realizedPnL = realizedPnL.add(positionRealizedPnL);
            totalInvestedCapital = totalInvestedCapital.add(entryCost);

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

            totalProfitLoss = totalProfitLoss.add(positionRealizedPnL);
        }

        // Calculate total profit/loss percentage (avoid div by zero)
        BigDecimal totalProfitLossPercentage = BigDecimal.ZERO;
        if (totalInvestedCapital.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercentage = totalProfitLoss.divide(totalInvestedCapital, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        instrumentReport.setInstrument(instrument);
        instrumentReport.setCurrentInvestedCapital(holding.getCurrentInvestedCapital());
        instrumentReport.setMaxInvestedCapital(holding.getMaxInvestedCapital());
        instrumentReport.setTotalInvestedCapital(totalInvestedCapital);
        instrumentReport.setRealizedPnL(realizedPnL);

        instrumentReport.setProfitCount(profitCount);
        instrumentReport.setBreakEvenCount(breakEvenCount);
        instrumentReport.setLossCount(lossCount);

        instrumentReport.setTotalProfitLoss(totalProfitLoss);
        instrumentReport.setTotalProfitLossPercentage(totalProfitLossPercentage);
        instrumentReport.setTotalProfit(totalProfit);
        instrumentReport.setTotalLoss(totalLoss);

        return instrumentReport;
    }

}
