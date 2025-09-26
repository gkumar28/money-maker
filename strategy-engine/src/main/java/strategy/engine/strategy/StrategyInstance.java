package strategy.engine.strategy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import strategy.engine.adapter.TA4JAdapter;
import strategy.engine.constant.enums.TradeAction;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.factory.IndicatorFactory;
import strategy.engine.factory.RuleFactory;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.analysis.TradingRecord;

import java.math.BigDecimal;

@Getter
@ToString
@EqualsAndHashCode
@Slf4j
public class StrategyInstance {

    private final String instrument;
    private final String strategyName;
    private final String interval;
    private final int delay;

    private final Rule entryRule;
    private final Rule expandRule;
    private final Rule trimRule;
    private final Rule exitRule;
    private final TradingRecord tradingRecord;
    private final BarSeries barSeries;

    private final ATRIndicator atr;
    private final ADXIndicator adx;

    public StrategyInstance(String instrument, StrategyDefinition strategyDefinition, TradingRecord tradingRecord, BarSeries barSeries) {
        IndicatorFactory indicatorFactory = new IndicatorFactory(barSeries, barSeries.num());
        RuleFactory ruleFactory = new RuleFactory(barSeries, indicatorFactory);

        this.instrument = instrument;
        this.strategyName = strategyDefinition.getName();
        this.interval = strategyDefinition.getInterval();
        this.delay = strategyDefinition.getDelay();

        this.entryRule = ruleFactory.create(strategyDefinition.getEntry());
        this.expandRule = ruleFactory.create(strategyDefinition.getExpand());
        this.trimRule = ruleFactory.create(strategyDefinition.getTrim());
        this.exitRule = ruleFactory.create(strategyDefinition.getExit());

        this.tradingRecord = tradingRecord;
        this.barSeries = barSeries;

        this.atr = new ATRIndicator(barSeries, 14);
        this.adx = new ADXIndicator(barSeries, 14);
    }


    public Signal evaluate(int index) {
        org.ta4j.core.TradingRecord ta4jTradingRecord = TA4JAdapter.asTradingRecord(tradingRecord);
        BigDecimal currentQuantity = tradingRecord.getOpenPosition().getOpenQuantity();
        boolean quantityGreaterThanZero = currentQuantity.compareTo(BigDecimal.ZERO) > 0;
        boolean entry = null != entryRule && !quantityGreaterThanZero && entryRule.isSatisfied(index, ta4jTradingRecord);
        boolean add = null != expandRule && quantityGreaterThanZero && expandRule.isSatisfied(index, ta4jTradingRecord);
        boolean trim = null != trimRule && quantityGreaterThanZero && trimRule.isSatisfied(index, ta4jTradingRecord);
        boolean exit = null != exitRule  && quantityGreaterThanZero && exitRule.isSatisfied(index, ta4jTradingRecord);

        TradeType tradeType = null;
        TradeAction tradeAction = null;

        if (index <= delay) {
            // do nothing as unstable
        } else if (entry) {
            tradeType = TradeType.BUY;
            tradeAction = TradeAction.ENTRY;
            logDebugMessage("ENTRY", index);
        } else if (add) {
            tradeType = TradeType.BUY;
            tradeAction = TradeAction.EXPAND;
            logDebugMessage("EXPAND", index);
        } else if (exit) {
            tradeType = TradeType.SELL;
            tradeAction = TradeAction.EXIT;
            logDebugMessage("EXIT", index);
        } else if (trim) {
            tradeType = TradeType.SELL;
            tradeAction = TradeAction.TRIM;
            logDebugMessage("TRIM", index);
        }

        return new Signal(
            tradeType,
            tradeAction,
            barSeries.getBar(index).getEndTime(),
            barSeries.getBar(index).getClosePrice().bigDecimalValue(),
            atr.getValue(index).bigDecimalValue(),
            adx.getValue(index).bigDecimalValue()
        );
    }

    private void logDebugMessage(String signal, int index) {
        if (log.isDebugEnabled()) {
            log.debug("{}: {} at index {}", instrument, signal, index);
        }
    }
}
