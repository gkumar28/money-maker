package strategy.engine.schemaobject.analysis;

import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

import java.util.List;

public interface ExtendedTradingRecord extends TradingRecord {

    Num getCurrentInvestedCapital();

    List<Trade> getOpenPositions();

    Num getRealizedCapitalFromPartialPosition();

    Num getRealizedProfitLossFromPartialPosition();

}
