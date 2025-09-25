package strategy.engine.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleDefinition;
import strategy.engine.validation.ValidRuleDefinition;

public class LogicalRuleDefinitionValidator implements ConstraintValidator<ValidRuleDefinition, LogicalRuleDefinition> {
    @Override
    public boolean isValid(LogicalRuleDefinition value, ConstraintValidatorContext context) {
        return false;
    }
}
