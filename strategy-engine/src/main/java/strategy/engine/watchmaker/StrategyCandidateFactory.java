package strategy.engine.watchmaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uncommons.watchmaker.framework.CandidateFactory;
import strategy.engine.strategy.StrategyDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Slf4j
public class StrategyCandidateFactory implements CandidateFactory<StrategyDefinition> {

    @Override
    public List<StrategyDefinition> generateInitialPopulation(int populationSize, Random rng) {
        return List.of();
    }

    @Override
    public List<StrategyDefinition> generateInitialPopulation(int populationSize, Collection<StrategyDefinition> seedCandidates, Random rng) {
        return List.of();
    }

    @Override
    public StrategyDefinition generateRandomCandidate(Random rng) {
        return null;
    }
}
