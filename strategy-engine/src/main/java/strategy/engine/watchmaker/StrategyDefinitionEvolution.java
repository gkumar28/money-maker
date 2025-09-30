package strategy.engine.watchmaker;

import lombok.extern.slf4j.Slf4j;
import org.uncommons.watchmaker.framework.CachingFitnessEvaluator;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionStrategyEngine;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.TargetFitness;
import strategy.engine.service.BacktestService;
import strategy.engine.strategy.StrategyDefinition;
import strategy.engine.watchmaker.operator.LeafToLeafRuleMutation;
import strategy.engine.watchmaker.operator.LeafToLogicalRuleMutation;
import strategy.engine.watchmaker.operator.LogicalToLeafRuleMutation;
import strategy.engine.watchmaker.operator.LogicalToLogicalRuleMutation;
import strategy.engine.watchmaker.operator.MultiSplitEvolution;
import strategy.engine.watchmaker.operator.StrategyDefinitionCrossOver;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
public class StrategyDefinitionEvolution {

    private final CandidateFactory<StrategyDefinition> candidateFactory;
    private final EvolutionaryOperator<StrategyDefinition> evolutionScheme;
    private final FitnessEvaluator<StrategyDefinition> fitnessEvaluator;
    private final boolean plusSelection;

    public StrategyDefinitionEvolution(BacktestService backtestService, List<String> instruments, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        IndicatorDefinitionGenerator indicatorDefinitionGenerator = new IndicatorDefinitionGenerator();
        RuleDefinitionGenerator ruleDefinitionGenerator = new RuleDefinitionGenerator(indicatorDefinitionGenerator);
        candidateFactory = new StrategyCandidateFactory(ruleDefinitionGenerator);
        evolutionScheme = new EvolutionPipeline<>(List.of(
            new StrategyDefinitionCrossOver(1),
            new MultiSplitEvolution(
                List.of(new LeafToLeafRuleMutation(ruleDefinitionGenerator),
                        new LeafToLogicalRuleMutation(ruleDefinitionGenerator),
                        new LogicalToLeafRuleMutation(ruleDefinitionGenerator),
                        new LogicalToLogicalRuleMutation(ruleDefinitionGenerator)),
                new double[]{0.25, 0.25, 0.25, 0.25})));
        FitnessEvaluator<StrategyDefinition> strategyFitnessEvaluator = new StrategyFitnessEvaluator(backtestService, instruments, exchange, interval, fromDate, toDate);
        fitnessEvaluator = new CachingFitnessEvaluator<>(strategyFitnessEvaluator);
        plusSelection = true;
    }

    public StrategyDefinition evolve() {

        EvolutionEngine<StrategyDefinition> evolutionEngine = new EvolutionStrategyEngine<>(
            candidateFactory,
            evolutionScheme,
            fitnessEvaluator,
            plusSelection,
            1,
            new Random());
        evolutionEngine.addEvolutionObserver(new StrategyDefinitionEvolutionObserver());

        List<EvaluatedCandidate<StrategyDefinition>> result = evolutionEngine.evolvePopulation(
            10,
            0,
            new GenerationCount(10),
            new TargetFitness(150.0, true));

        return result.get(0).getCandidate();
    }
}
