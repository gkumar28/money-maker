package strategy.engine.constant.enums;

public enum TradeType {

    BUY {
        @Override
        public TradeType complementType() {
            return SELL;
        }
    },

    /** A SELL corresponds to an <i>ASK</i> trade. */
    SELL {
        @Override
        public TradeType complementType() {
            return BUY;
        }
    };

    public abstract TradeType complementType();
}
