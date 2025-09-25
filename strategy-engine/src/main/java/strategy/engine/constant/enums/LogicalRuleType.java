package strategy.engine.constant.enums;

import strategy.engine.schemaobject.strategy.tree.LogicalRuleSignature;
import strategy.engine.schemaobject.strategy.tree.RuleSignature;

import java.util.List;

public enum LogicalRuleType implements RuleType {
    NOT {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LogicalRuleSignature.Types.UNARY.getSignature());
        }
    },
    AND {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LogicalRuleSignature.Types.BINARY.getSignature());
        }
    },
    OR {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LogicalRuleSignature.Types.BINARY.getSignature());
        }
    },
    XOR {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LogicalRuleSignature.Types.BINARY.getSignature());
        }
    };

}
