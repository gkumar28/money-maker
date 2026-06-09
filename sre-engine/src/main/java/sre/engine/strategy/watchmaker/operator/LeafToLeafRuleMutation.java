package sre.engine.strategy.watchmaker.operator;

import sre.engine.strategy.schemaobject.strategy.tree.LeafRuleDefinition;
import sre.engine.strategy.schemaobject.strategy.tree.RuleDefinition;
import sre.engine.strategy.watchmaker.EvolutionUtil;
import sre.engine.strategy.watchmaker.RuleDefinitionGenerator;

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
