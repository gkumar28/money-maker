package strategy.engine.watchmaker.operator;

import strategy.engine.constant.enums.LogicalRuleType;
import strategy.engine.schemaobject.strategy.tree.LeafRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.watchmaker.EvolutionUtil;
import strategy.engine.watchmaker.RuleDefinitionGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LogicalToLogicalRuleMutation extends AbstractStrategyMutation {

    public LogicalToLogicalRuleMutation(RuleDefinitionGenerator ruleDefinitionGenerator) {
        super(ruleDefinitionGenerator);
    }

    @Override
    protected RuleDefinition sample(RuleDefinition root, Random rng) {
        return EvolutionUtil.sample(root, rng, LogicalRuleDefinition.class);
    }

    @Override
    protected RuleDefinition mutate(RuleDefinition ruleDefinition, Random rng) {
        double chance = rng.nextDouble();
        LogicalRuleDefinition logicalRuleDefinition = (LogicalRuleDefinition) ruleDefinition;
        LogicalRuleType ruleType = (LogicalRuleType) logicalRuleDefinition.getRuleType();
        List<LogicalRuleType> possibleRuleTypes = new ArrayList<>(List.of(LogicalRuleType.values()));
        List<RuleDefinition> children = new ArrayList<>(logicalRuleDefinition.getChildren());
        if (chance < 0.5) {
            possibleRuleTypes.remove(ruleType);
            ruleType = possibleRuleTypes.get(rng.nextInt(3));
        } else {
            children.add(ruleDefinitionGenerator.generate(LeafRuleDefinition.class, rng));
        }

        if (LogicalRuleType.NOT.equals(ruleType)) {
            Collections.shuffle(children, rng);
            return new LogicalRuleDefinition(ruleType, List.of(ruleDefinition));
        }

        return new LogicalRuleDefinition(ruleType, children);
    }
}
