package sre.engine.strategy.service.impl;

import common.lib.utils.GenericUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.*;
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.reports.TradingStatement;
import sre.engine.strategy.indicator.KallmanIndicator;
import sre.engine.strategy.schemaobject.barseries.BatchedBarSeriesManager;
import sre.engine.strategy.schemaobject.barseries.provider.FileBarDataProvider;
import sre.engine.strategy.schemaobject.barseries.LookAheadBarSeriesBuilder;
import sre.engine.strategy.service.StrategyBacktestService;
import sre.engine.strategy.service.MarketDataService;
import sre.engine.strategy.strategy.SignalStrategy;
import sre.engine.strategy.strategy.StrategyDefinition;
import sre.engine.strategy.strategy.StrategyDefinitionParser;
import sre.engine.strategy.strategy.DiscreteSignalStrategy;

import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class StrategyBacktestServiceImpl implements StrategyBacktestService {


    private final MarketDataService marketDataService;
    private final StrategyDefinitionParser strategyDefinitionParser;

    @Override
    public TradingStatement backtest(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        StrategyDefinition strategyDefinition = strategyDefinitionParser.readAny();
        return backtest(instrument, exchange, interval, fromDate, toDate, true, strategyDefinition);
    }

    @Override
    public TradingStatement backtest(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate, boolean writeResultToFile, StrategyDefinition strategyDefinition) {
        // basic setup
        LocalDateTime from = fromDate.atTime(LocalTime.of(0, 0, 0));
        LocalDateTime to = toDate.atTime(LocalTime.of(23, 59, 59));
        if (null != interval) {
            strategyDefinition = strategyDefinition.withInterval(interval);
        }

        // load data
        Path dataFilePath = marketDataService.loadRawData(instrument, exchange, from, to, interval);
        BarSeries barSeries = getBacktestBarSeries(instrument, dataFilePath, interval);
        SignalStrategy strategy = new DiscreteSignalStrategy(strategyDefinition, barSeries);

        TradeExecutionModel executionModel = new SlippageExecutionModel(DecimalNumFactory.getInstance().numOf(0.01),
                TradeExecutionModel.PriceSource.NEXT_OPEN);
        BarSeriesManager barSeriesManager = new BatchedBarSeriesManager(1, barSeries, executionModel);
        BacktestExecutor backtestExecutor = new BacktestExecutor(barSeriesManager);
        // execute
        List<TradingStatement> result = backtestExecutor.execute(List.of(strategy), barSeries.numFactory().thousand());
        log.debug("\n================================ END OF BACK TEST =================================\n");
        return result.getFirst();
    }

    private BarSeries getBacktestBarSeries(String instrument, Path path, String interval) {
        return new LookAheadBarSeriesBuilder()
                .withName(instrument)
                .withNumFactory(DecimalNumFactory.getInstance())
                .withMaxBarCount(2000)
                .withSeriesBeginIndex(0)
                .withBarBuilderFactory(new TimeBarBuilderFactory(GenericUtils.getDuration(interval)))
                .withDataProvider(new FileBarDataProvider(path))
                .build();
    }



    @Override
    public List<Map<String, Object>> getIndicatorValues(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atTime(LocalTime.of(0, 0, 0));
        LocalDateTime to = toDate.atTime(LocalTime.of(23, 59, 59));
        Path dataFilePath = marketDataService.loadRawData(instrument, exchange, from, to, interval);
        List<Map<String, Object>> result = new ArrayList<>();

        try(Stream<String> lines = Files.lines(dataFilePath)) {

            BarSeries barSeries = new BaseBarSeriesBuilder()
                    .withName(instrument + ":backtest")
                    .withMaxBarCount(100)
                    .withNumFactory(DecimalNumFactory.getInstance())
                    .build();

            ClosePriceIndicator close = new ClosePriceIndicator(barSeries);
            KallmanIndicator kallman = new KallmanIndicator(close, 0.1, 5);

            Iterator<String> fileIterator = lines.skip(1).iterator();
            int index = 0;
            while(fileIterator.hasNext()) {
                Map<String, Object> data = new HashMap<>();
                data.put("closePrice", close.getValue(index).bigDecimalValue());
                data.put("kallmanPrice", kallman.getValue(index).bigDecimalValue());
                data.put("% value", close.getValue(index).bigDecimalValue().divide(kallman.getValue(index).bigDecimalValue(), 2, RoundingMode.HALF_UP));

                result.add(data);
                index++;
            }
        } catch (Exception exception) {
            log.error("Backtest failed", exception);
        }

        return result;
    }


}
