package money.maker.service;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.ZonedDateTime;

public interface RedisService {

    String getNewBarData(String instrument, ZonedDateTime timestamp);

    String getNewTickData(String instrument, ZonedDateTime timestamp);

    BarSeries initInstrument(String instrument, int barCount);

    void updateInstrument(String instrument, Bar bar);
}
