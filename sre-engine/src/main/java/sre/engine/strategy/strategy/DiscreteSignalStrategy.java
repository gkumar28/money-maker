package sre.engine.strategy.strategy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import sre.engine.strategy.factory.IndicatorFactory;
import sre.engine.strategy.factory.RuleFactory;
import sre.engine.strategy.schemaobject.signal.Signal;
import sre.engine.strategy.schemaobject.signal.SignalContext;
import sre.engine.strategy.schemaobject.signal.SignalMetaData;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@Slf4j
public class DiscreteSignalStrategy implements SignalStrategy {

    private final String strategyName;
    private final String interval;
    private int unstableBars;
    private final BarSeries barSeries;

    private final Rule entryRule;
    private final Rule expandRule;
    private final Rule trimRule;
    private final Rule exitRule;
    private final NumFactory numFactory;

    // indicators
    private final ATRIndicator atr;
    private final ADXIndicator adx;
    private final ClosePriceIndicator price;

    public DiscreteSignalStrategy(String name, String interval, int unstableBars, BarSeries barSeries, Rule entryRule, Rule expandRule, Rule trimRule, Rule exitRule) {
        this.strategyName = name;
        this.interval = interval;
        this.unstableBars = unstableBars;
        this.barSeries = barSeries;
        this.numFactory = barSeries.numFactory();

        this.entryRule = entryRule;
        this.expandRule = expandRule;
        this.trimRule = trimRule;
        this.exitRule = exitRule;

        this.atr = new ATRIndicator(barSeries, 14);
        this.adx = new ADXIndicator(barSeries, 14);
        this.price = new ClosePriceIndicator(barSeries);
    }

    public DiscreteSignalStrategy(StrategyDefinition strategyDefinition, BarSeries barSeries) {
        IndicatorFactory indicatorFactory = new IndicatorFactory(barSeries);
        RuleFactory ruleFactory = new RuleFactory(barSeries, indicatorFactory);

        this.strategyName = strategyDefinition.getName();
        this.interval = strategyDefinition.getInterval();
        this.unstableBars = strategyDefinition.getDelay();
        this.barSeries = barSeries;
        this.numFactory = barSeries.numFactory();

        this.entryRule = ruleFactory.create(strategyDefinition.getEntry());
        this.expandRule = ruleFactory.create(strategyDefinition.getExpand());
        this.trimRule = ruleFactory.create(strategyDefinition.getTrim());
        this.exitRule = ruleFactory.create(strategyDefinition.getExit());

        this.atr = new ATRIndicator(barSeries, 14);
        this.adx = new ADXIndicator(barSeries, 14);
        this.price = new ClosePriceIndicator(barSeries);
    }


    @Override
    public SignalContext operate(int index, TradingRecord tradingRecord) {
        if (null == tradingRecord) { return SignalContext.instance(); }

        Position position = tradingRecord.getCurrentPosition();

        if (position.isNew() && getEntryRule().isSatisfied(index, tradingRecord)) {
            return createSignal(index, numFactory.numOf(0.4));
        }

        if (position.isOpened() && getExpandRule().isSatisfied(index, tradingRecord)) {
            return createSignal(index, numFactory.numOf(0.7));
        }

        if (position.isOpened() && getTrimRule().isSatisfied(index, tradingRecord)) {
            return createSignal(index, numFactory.numOf(0.3));
        }

        if (position.isOpened() && getExitRule().isSatisfied(index, tradingRecord)) {
            return createSignal(index, numFactory.numOf(0.0));
        }

        return createSignal(index, numFactory.thousand());
    }

    private SignalContext createSignal(int index, Num alpha) {
        if (log.isDebugEnabled()) {
            log.debug("{}: exposure: {} at index {}", barSeries.getName(), alpha.bigDecimalValue(), index);
        }
        return SignalContext.instance()
                .withSignal(new Signal(UUID.randomUUID().toString(), index, barSeries.getBar(index).getEndTime(), alpha.bigDecimalValue()))
                .withMetaData(new SignalMetaData(atr.getValue(index).bigDecimalValue(), adx.getValue(index).bigDecimalValue(), price.getValue(index).bigDecimalValue()));
    }

    @Override
    public String getName() {
        return strategyName;
    }

    @Override
    public Strategy and(String name, Strategy strategy, int unstableBars) {
        if (strategy instanceof DiscreteSignalStrategy discreteSignalStrategy) {
            if (!getInterval().equals(discreteSignalStrategy.getInterval())) {
                throw new UnsupportedOperationException("Interval of both strategies must be same");
            }

            if (!getBarSeries().equals(discreteSignalStrategy.getBarSeries())) {
                throw new UnsupportedOperationException("Underlying bar series must be same");
            }

            return new DiscreteSignalStrategy(
                    "and(" + strategyName + "," + strategy.getName() + ")",
                    interval,
                    Math.max(unstableBars, discreteSignalStrategy.getUnstableBars()),
                    barSeries,
                    getEntryRule().and(discreteSignalStrategy.getEntryRule()),
                    getExpandRule().and(discreteSignalStrategy.getExpandRule()),
                    getTrimRule().and(discreteSignalStrategy.getTrimRule()),
                    getExitRule().and(discreteSignalStrategy.getExitRule())
            );
        }

        throw new UnsupportedOperationException("Strategy must be of the type : DiscreteSignalStrategy");
    }

    @Override
    public Strategy or(String name, Strategy strategy, int unstableBars) {
        if (strategy instanceof DiscreteSignalStrategy discreteSignalStrategy) {
            if (!getInterval().equals(discreteSignalStrategy.getInterval())) {
                throw new UnsupportedOperationException("Interval of both strategies must be same");
            }

            if (!getBarSeries().equals(discreteSignalStrategy.getBarSeries())) {
                throw new UnsupportedOperationException("Underlying bar series must be same");
            }

            return new DiscreteSignalStrategy(
                    "and(" + strategyName + "," + strategy.getName() + ")",
                    interval,
                    Math.max(unstableBars, discreteSignalStrategy.getUnstableBars()),
                    barSeries,
                    getEntryRule().or(discreteSignalStrategy.getEntryRule()),
                    getExpandRule().or(discreteSignalStrategy.getExpandRule()),
                    getTrimRule().or(discreteSignalStrategy.getTrimRule()),
                    getExitRule().or(discreteSignalStrategy.getExitRule())
            );
        }

        throw new UnsupportedOperationException("Strategy must be of the type : DiscreteSignalStrategy");
    }

    @Override
    public Strategy opposite() {
        return new DiscreteSignalStrategy(strategyName, interval, unstableBars, barSeries, exitRule, trimRule, expandRule, entryRule);
    }

    @Override
    public void setUnstableBars(int unstableBars) {
        this.unstableBars = unstableBars;
    }

    @Override
    public boolean isUnstableAt(int index) {
        return unstableBars >= index;
    }
}
