package sre.engine.strategy.watchmaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.reports.TradingStatement;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import sre.engine.strategy.service.StrategyBacktestService;
import sre.engine.strategy.strategy.StrategyDefinition;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class StrategyFitnessEvaluator implements FitnessEvaluator<StrategyDefinition> {

    private final StrategyBacktestService strategyBacktestService;
    private final String instrument;
    private final String exchange;
    private final String interval;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    @Override
    public double getFitness(StrategyDefinition candidate, List<? extends StrategyDefinition> population) {
        TradingStatement tradingStatement = strategyBacktestService.backtest(
            instrument,
            exchange,
            interval,
            fromDate,
            toDate,
            false,
            candidate);
        return 100.0 + tradingStatement.getPerformanceReport().totalProfitLossPercentage.doubleValue();
    }

    @Override
    public boolean isNatural() {
        return true;
    }
}
