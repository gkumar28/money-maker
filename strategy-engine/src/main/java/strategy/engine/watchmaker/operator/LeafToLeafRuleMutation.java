package strategy.engine.watchmaker.operator;

import strategy.engine.schemaobject.strategy.tree.LeafRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.watchmaker.EvolutionUtil;
import strategy.engine.watchmaker.RuleDefinitionGenerator;

import java.util.Random;

public class LeafToLeafRuleMutation extends AbstractStrategyMutation {

    public LeafToLeafRuleMutation(RuleDefinitionGenerator ruleDefinitionGenerator) {
        super(ruleDefinitionGenerator);
    }

    @Override
    protected RuleDefinition sample(RuleDefinition root, Random rng) {
        return EvolutionUtil.sample(root, rng, LeafRuleDefinition.class);
    }

    @Override
    protected RuleDefinition mutate(RuleDefinition ruleDefinition, Random rng) {
        double chance = rng.nextDouble();

        if (chance < 0.8) {
            return ruleDefinitionGenerator.generate(ruleDefinition.getRuleType(), rng);
        }

        return ruleDefinitionGenerator.generate(LeafRuleDefinition.class, rng);
    }
}
