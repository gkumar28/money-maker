package strategy.engine.watchmaker.operator;

import lombok.RequiredArgsConstructor;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.strategy.StrategyDefinition;
import strategy.engine.watchmaker.EvolutionUtil;
import strategy.engine.watchmaker.RuleDefinitionGenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class AbstractStrategyMutation implements EvolutionaryOperator<StrategyDefinition> {

    protected final RuleDefinitionGenerator ruleDefinitionGenerator;

    @Override
    public List<StrategyDefinition> apply(List<StrategyDefinition> selectedCandidates, Random rng) {
        List<StrategyDefinition> offsprings = new ArrayList<>();
        int ruleToMutate = rng.nextInt(4);
        for (StrategyDefinition candidate: selectedCandidates) {
            StrategyDefinition.StrategyDefinitionBuilder builder = StrategyDefinition.builder();
            offsprings.add(builder
                .name(UUID.randomUUID().toString())
                .interval(candidate.getInterval())
                .delay(candidate.getDelay())
                .entry(mutateRule(candidate.getEntry(), ruleToMutate == 0, rng))
                .expand(mutateRule(candidate.getExpand(), ruleToMutate == 1, rng))
                .trim(mutateRule(candidate.getTrim(), ruleToMutate == 2, rng))
                .exit(mutateRule(candidate.getExit(), ruleToMutate ==3, rng))
                .build());
        }
        return offsprings;
    }

    private RuleDefinition mutateRule(RuleDefinition ruleDefinition, boolean mutate, Random rng) {
        if (!mutate) {
            return ruleDefinition;
        }

        RuleDefinition selected = sample(ruleDefinition, rng);
        // if sampling not possible due to class constraints
        if (selected == null) {
            return ruleDefinition;
        }

        RuleDefinition target = mutate(selected, rng);
        return EvolutionUtil.swap(ruleDefinition, selected, target);
    }

    protected abstract RuleDefinition sample(RuleDefinition root, Random rng);
    protected abstract RuleDefinition mutate(RuleDefinition ruleDefinition, Random rng);
}
