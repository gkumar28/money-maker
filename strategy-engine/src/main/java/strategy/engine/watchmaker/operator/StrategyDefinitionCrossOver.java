package strategy.engine.watchmaker.operator;

import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.strategy.StrategyDefinition;
import strategy.engine.watchmaker.EvolutionUtil;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class StrategyDefinitionCrossOver extends AbstractCrossover<StrategyDefinition> {

    public StrategyDefinitionCrossOver(int crossoverPoints) {
        super(crossoverPoints);
    }

    public StrategyDefinitionCrossOver(int crossoverPoints, Probability crossoverProbability) {
        super(crossoverPoints, crossoverProbability);
    }

    @Override
    protected List<StrategyDefinition> mate(StrategyDefinition parent1, StrategyDefinition parent2, int numberOfCrossoverPoints, Random rng) {
        List<RuleDefinition> entryOffspring = mateRuleDefinitions(parent1.getEntry(), parent2.getEntry(), rng);
        List<RuleDefinition> expandOffspring = mateRuleDefinitions(parent1.getExpand(), parent2.getExpand(), rng);
        List<RuleDefinition> trimOffspring = mateRuleDefinitions(parent1.getTrim(), parent2.getTrim(), rng);
        List<RuleDefinition> exitOffspring = mateRuleDefinitions(parent1.getExit(), parent2.getExit(), rng);
        String interval = parent1.getInterval();
        int delay = parent1.getDelay();
        return List.of(
            new StrategyDefinition(UUID.randomUUID().toString(), interval, delay, entryOffspring.get(0), exitOffspring.get(0), expandOffspring.get(0), trimOffspring.get(0)),
            new StrategyDefinition(UUID.randomUUID().toString(), interval, delay, entryOffspring.get(1), exitOffspring.get(1), expandOffspring.get(1), trimOffspring.get(1))
        );
    }

    private List<RuleDefinition> mateRuleDefinitions(RuleDefinition rule1, RuleDefinition rule2, Random rng) {
        RuleDefinition sample1 = EvolutionUtil.sample(rule1, rng, RuleDefinition.class);
        RuleDefinition sample2 = EvolutionUtil.sample(rule2, rng, RuleDefinition.class);

        RuleDefinition offspring1 = EvolutionUtil.swap(rule1, sample1, sample2);
        RuleDefinition offspring2 = EvolutionUtil.swap(rule2, sample2, sample1);

        return List.of(offspring1, offspring2);
    }






}
