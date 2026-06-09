package sre.engine.strategy.watchmaker.operator;

import sre.engine.strategy.schemaobject.strategy.tree.LeafRuleDefinition;
import sre.engine.strategy.schemaobject.strategy.tree.LogicalRuleDefinition;
import sre.engine.strategy.schemaobject.strategy.tree.RuleDefinition;
import sre.engine.strategy.watchmaker.EvolutionUtil;
import sre.engine.strategy.watchmaker.RuleDefinitionGenerator;

import java.util.Random;

public class LogicalToLeafRuleMutation extends AbstractStrategyMutation {

    public LogicalToLeafRuleMutation(RuleDefinitionGenerator ruleDefinitionGenerator) {
        super(ruleDefinitionGenerator);
    }

    @Override
    protected RuleDefinition sample(RuleDefinition root, Random rng) {
        return EvolutionUtil.sample(root, rng, LogicalRuleDefinition.class);
    }

    @Override
    protected RuleDefinition mutate(RuleDefinition ruleDefinition, Random rng) {
        return EvolutionUtil.sample(ruleDefinition, rng, LeafRuleDefinition.class);
    }
}
