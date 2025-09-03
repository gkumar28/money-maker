package execution.engine.service;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.util.List;

public interface RedisService {

    Bar getBar(String instrument, String timestamp);
    BarSeries getAllBars(String instrument, List<String> timestamps);
    BarSeries getNBars(String instrument, int n, int offset);
}
