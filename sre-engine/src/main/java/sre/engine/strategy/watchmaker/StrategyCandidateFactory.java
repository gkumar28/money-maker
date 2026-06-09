package sre.engine.strategy.watchmaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uncommons.watchmaker.framework.CandidateFactory;
import sre.engine.strategy.strategy.StrategyDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class StrategyCandidateFactory implements CandidateFactory<StrategyDefinition> {

    private final RuleDefinitionGenerator ruleDefinitionGenerator;

    @Override
    public List<StrategyDefinition> generateInitialPopulation(int populationSize, Collection<StrategyDefinition> seedCandidates, Random rng) {
        if (seedCandidates == null) {
            return generateInitialPopulation(populationSize, rng);
        }

        List<StrategyDefinition> candidateClone = new ArrayList<>(seedCandidates);

        if (seedCandidates.size() >= populationSize) {
            candidateClone = candidateClone.subList(0, populationSize);
        } else {
            candidateClone.addAll(generateInitialPopulation(populationSize - seedCandidates.size(), rng));
        }

        return candidateClone;
    }

    @Override
    public List<StrategyDefinition> generateInitialPopulation(int populationSize, Random rng) {
        List<StrategyDefinition> result = new ArrayList<>(populationSize);

        for(int i=0;i<populationSize;i++) {
            result.add(generateRandomCandidate(rng));
        }
        return result;
    }

    @Override
    public StrategyDefinition generateRandomCandidate(Random rng) {
        return StrategyDefinition.builder()
            .name(UUID.randomUUID().toString())
            .interval("minute")
            .delay(50)
            .entry(ruleDefinitionGenerator.generate(rng))
            .expand(ruleDefinitionGenerator.generate(rng))
            .trim(ruleDefinitionGenerator.generate(rng))
            .exit(ruleDefinitionGenerator.generate(rng))
            .build();
    }
}
