package strategy.engine.schemaobject.strategy.tree;

import lombok.Data;
import strategy.engine.constant.enums.RuleType;

@Data
public abstract class RuleDefinition {

    private final RuleType ruleType;
}
