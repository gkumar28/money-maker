package strategy.engine.watchmaker.operator;

import strategy.engine.schemaobject.strategy.tree.LeafRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.watchmaker.EvolutionUtil;
import strategy.engine.watchmaker.RuleDefinitionGenerator;

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
