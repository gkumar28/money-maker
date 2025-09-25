package strategy.engine.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import strategy.engine.validation.impl.RuleDefinitionValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RuleDefinitionValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRuleDefinition {
    String message() default "Invalid leaf rule definition";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
