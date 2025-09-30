package strategy.engine.watchmaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.service.BacktestService;
import strategy.engine.strategy.StrategyDefinition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class StrategyFitnessEvaluator implements FitnessEvaluator<StrategyDefinition> {

    private final BacktestService backtestService;
    private final List<String> instruments;
    private final String exchange;
    private final String interval;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    @Override
    public double getFitness(StrategyDefinition candidate, List<? extends StrategyDefinition> population) {
        TradingReport report = backtestService.backtest(
            instruments,
            exchange,
            interval,
            fromDate,
            toDate,
            false,
            candidate);
        return report.getProfitLossPercentage().doubleValue();
    }

    @Override
    public boolean isNatural() {
        return true;
    }
}
