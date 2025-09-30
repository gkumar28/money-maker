package strategy.engine.watchmaker;

import lombok.extern.slf4j.Slf4j;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;
import strategy.engine.strategy.StrategyDefinition;

@Slf4j
public class StrategyDefinitionEvolutionObserver implements EvolutionObserver<StrategyDefinition> {

    @Override
    public void populationUpdate(PopulationData<? extends StrategyDefinition> data) {
        log.info("Epoch {}: Best - {}, mean - {} population - {}",
            data.getGenerationNumber(),
            String.format("%.2f", data.getBestCandidateFitness()),
            String.format("%.2f", data.getMeanFitness()),
            data.getPopulationSize());
    }
}
