package sre.engine.strategy.watchmaker.operator;

import lombok.extern.slf4j.Slf4j;
import sre.engine.strategy.constant.enums.LogicalRuleType;
import sre.engine.strategy.schemaobject.strategy.tree.LeafRuleDefinition;
import sre.engine.strategy.schemaobject.strategy.tree.LogicalRuleDefinition;
import sre.engine.strategy.schemaobject.strategy.tree.RuleDefinition;
import sre.engine.strategy.watchmaker.EvolutionUtil;
import sre.engine.strategy.watchmaker.RuleDefinitionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class LeafToLogicalRuleMutation extends AbstractStrategyMutation {


    public LeafToLogicalRuleMutation(RuleDefinitionGenerator ruleDefinitionGenerator) {
        super(ruleDefinitionGenerator);
    }

    @Override
    protected RuleDefinition sample(RuleDefinition root, Random rng) {
        return EvolutionUtil.sample(root, rng, LeafRuleDefinition.class);
    }

    @Override
    protected RuleDefinition mutate(RuleDefinition ruleDefinition, Random rng) {
        LogicalRuleDefinition logicalRuleDefinition = (LogicalRuleDefinition) ruleDefinitionGenerator.generate(LogicalRuleDefinition.class, rng);
        List<RuleDefinition> newChildren = new ArrayList<>();
        newChildren.add(ruleDefinition);
        if (!logicalRuleDefinition.getRuleType().equals(LogicalRuleType.NOT)) {
            newChildren.addAll(logicalRuleDefinition.getChildren());
        }
        return logicalRuleDefinition.withChildren(newChildren);
    }


}
