package sre.engine.strategy.constant.enums;

import sre.engine.strategy.schemaobject.strategy.tree.RuleSignature;

import java.util.List;

public interface RuleType {

    List<RuleSignature> allowedSignatures();
}
