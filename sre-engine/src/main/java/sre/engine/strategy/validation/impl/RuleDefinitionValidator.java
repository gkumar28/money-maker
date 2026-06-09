package sre.engine.strategy.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import sre.engine.strategy.constant.enums.RuleType;
import sre.engine.strategy.schemaobject.strategy.tree.RuleDefinition;
import sre.engine.strategy.validation.ValidRuleDefinition;

@Slf4j
public class RuleDefinitionValidator implements ConstraintValidator<ValidRuleDefinition, RuleDefinition> {

    @Override
    public boolean isValid(RuleDefinition ruleDef, ConstraintValidatorContext context) {
        if (ruleDef == null) return true;

        RuleType type = ruleDef.getRuleType();

        if (type == null) {
            buildViolation(context, "Rule type must not be null");
            return false;
        }

        boolean noSignatureMatch = type.allowedSignatures().stream()
            .noneMatch(ruleSignature -> ruleSignature.matches(ruleDef));
        if (noSignatureMatch) {
            buildViolation(context, "No signatures are satisfied for this Rule type");
        }

        return noSignatureMatch;
    }

    private void buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
