package money.maker.service;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

public interface RedisService {

    BarSeries initInstrument(String instrument, int barCount);

    void updateInstrument(String instrument, Bar bar);

    void onNewBarUpdate(String instrument, String timestamp);
}
