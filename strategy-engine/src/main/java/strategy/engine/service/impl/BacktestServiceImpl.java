package strategy.engine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;
import strategy.engine.component.TradingStrategyFactory;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.analysis.ExtendedTradeExecutionModel;
import strategy.engine.schemaobject.analysis.MultiPositionTradeOnNextOpenModel;
import strategy.engine.schemaobject.analysis.MultiPositionTradingRecord;
import strategy.engine.service.BacktestService;
import strategy.engine.service.MarketDataService;
import strategy.engine.service.PortfolioService;
import strategy.engine.service.PositionManagementService;
import strategy.engine.strategy.TradingStrategy;

import java.math.BigDecimal;

import static strategy.engine.util.StrategyEngineUtils.asTradeType;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class BacktestServiceImpl implements BacktestService {


    private final MarketDataService marketDataService;
    private final PortfolioService portfolioService;
    private final PositionManagementService positionManagementService;
    private final TradingStrategyFactory tradingStrategyFactory;


    @Override
    public TradingStatement backtest(String instrument, StrategyType strategyType, String fromDate, String toDate) {
        // 1. Load historical data for the instrument
        portfolioService.resetPortfolio(BigDecimal.valueOf(100_000));
        BarSeries barSeries = marketDataService.loadHistoricalData(instrument, fromDate, toDate);
        TradingStrategy strategy = tradingStrategyFactory.create(strategyType, barSeries);
        TradingRecord tradingRecord = new MultiPositionTradingRecord(instrument, Trade.TradeType.BUY);
        ExtendedTradeExecutionModel tradeExecutionModel = new MultiPositionTradeOnNextOpenModel();

        for (int i = 0; i < barSeries.getBarCount(); i++) {
            SignalDto newSignal = strategy.evaluate(i);
            StrategyOrderDto strategyOrderDto = positionManagementService.createOrderForLongPosition(instrument, newSignal);

            if (null != newSignal.getDirection()) {
                tradeExecutionModel.execute(i, tradingRecord, barSeries, DecimalNum.valueOf(strategyOrderDto.getQuantity()), asTradeType(strategyOrderDto.getDirection()));
            }
        }

        return new TradingStatementGenerator().generate(null, tradingRecord, barSeries);
    }


}
