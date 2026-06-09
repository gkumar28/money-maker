package sre.engine.strategy.constant.enums;

import sre.engine.strategy.schemaobject.strategy.tree.LeafRuleSignature;
import sre.engine.strategy.schemaobject.strategy.tree.RuleSignature;

import java.util.List;

public enum LeafRuleType implements RuleType {

    OVER_INDICATOR {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(
                LeafRuleSignature.Types.TWO_INDICATORS.getSignature(),
                LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature());
        }
    },
    UNDER_INDICATOR {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(
                LeafRuleSignature.Types.TWO_INDICATORS.getSignature(),
                LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature());
        }
    },
    CROSSED_UP_INDICATOR {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(
                LeafRuleSignature.Types.TWO_INDICATORS.getSignature(),
                LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature(),
                LeafRuleSignature.Types.MACD_CROSSOVER.getSignature());
        }
    },
    CROSSED_DOWN_INDICATOR {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(
                LeafRuleSignature.Types.TWO_INDICATORS.getSignature(),
                LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature(),
                LeafRuleSignature.Types.MACD_CROSSOVER.getSignature());
        }
    },
    IS_EQUAL {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(
                LeafRuleSignature.Types.TWO_INDICATORS.getSignature(),
                LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature());
        }
    },
    IS_RISING {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.ONE_INDICATOR_WITH_BAR_COUNT_AND_THRESHOLD.getSignature());
        }
    },
    IS_FALLING {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.ONE_INDICATOR_WITH_BAR_COUNT_AND_THRESHOLD.getSignature());
        }
    },
    WITHIN_PERCENTAGE {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(
                LeafRuleSignature.Types.TWO_INDICATORS_WITH_PERCENTAGE.getSignature(),
                LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD_AND_PERCENTAGE.getSignature());
        }
    },
    STOP_LOSS {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.CLOSE_PRICE_WITH_PERCENTAGE.getSignature());
        }
    },
    STOP_GAIN {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.CLOSE_PRICE_WITH_PERCENTAGE.getSignature());
        }
    },
    TRAILING_STOP_LOSS {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.PRICE_WITH_PERCENTAGE_AND_BAR_COUNT.getSignature());
        }
    },
    AVERAGE_TRUE_RANGE_STOP_GAIN {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.PRICE_WITH_ATR_BAR_COUNT_AND_COEFFICIENT.getSignature());
        }
    },
    AVERAGE_TRUE_RANGE_STOP_LOSS {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.PRICE_WITH_ATR_BAR_COUNT_AND_COEFFICIENT.getSignature());
        }
    },
    AVERAGE_TRUE_RANGE_TRAILING_STOP_LOSS {
        @Override
        public List<RuleSignature> allowedSignatures() {
            return List.of(LeafRuleSignature.Types.PRICE_WITH_ATR_BAR_COUNT_AND_COEFFICIENT.getSignature());
        }
    }

}
