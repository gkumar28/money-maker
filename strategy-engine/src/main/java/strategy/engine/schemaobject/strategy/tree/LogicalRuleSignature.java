package strategy.engine.schemaobject.strategy.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogicalRuleSignature extends RuleSignature {

    private final int childCount;

    @Override
    public boolean matches(RuleDefinition ruleDefinition) {
        if (!(ruleDefinition instanceof LogicalRuleDefinition logicalRuleDefinition)) {
            return false;
        }

        int actualChildCount = 0;
        if (null != logicalRuleDefinition.getLeft()) actualChildCount++;
        if (null != logicalRuleDefinition.getRight()) actualChildCount++;

        return actualChildCount != this.childCount;
    }

    @RequiredArgsConstructor
    @Getter
    public enum Types {
        BINARY(new LogicalRuleSignature(2)),
        UNARY(new LogicalRuleSignature(1));

        private final RuleSignature signature;
    }
}
