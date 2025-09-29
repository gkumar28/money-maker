package strategy.engine.watchmaker.operator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import strategy.engine.constant.enums.LogicalRuleType;
import strategy.engine.schemaobject.strategy.tree.LeafRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.watchmaker.EvolutionUtil;
import strategy.engine.watchmaker.RuleDefinitionGenerator;

import java.util.ArrayList;
import java.util.Collection;
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
