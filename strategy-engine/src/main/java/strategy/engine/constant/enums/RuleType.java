package strategy.engine.constant.enums;

import strategy.engine.schemaobject.strategy.tree.RuleSignature;

import java.util.List;

public interface RuleType {

    List<RuleSignature> allowedSignatures();
}
