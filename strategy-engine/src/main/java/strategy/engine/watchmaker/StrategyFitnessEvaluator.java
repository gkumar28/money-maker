package strategy.engine.watchmaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.service.BacktestService;
import strategy.engine.strategy.StrategyDefinition;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class StrategyFitnessEvaluator implements FitnessEvaluator<StrategyDefinition> {

    private final BacktestService backtestService;

    @Override
    public double getFitness(StrategyDefinition candidate, List<? extends StrategyDefinition> population) {
        TradingReport report = backtestService.backtest(
            List.of("RELIANCE"),
            "NSE",
            "minute",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 9, 30),
            candidate);
        return report.getProfitLossPercentage().doubleValue();
    }

    @Override
    public boolean isNatural() {
        return true;
    }
}
