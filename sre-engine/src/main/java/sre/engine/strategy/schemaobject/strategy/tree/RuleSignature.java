package sre.engine.strategy.schemaobject.strategy.tree;

public abstract class RuleSignature {

    public abstract boolean matches(RuleDefinition ruleDefinition);
}
