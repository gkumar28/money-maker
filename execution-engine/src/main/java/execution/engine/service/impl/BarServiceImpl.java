package execution.engine.service.impl;

import execution.engine.cache.InstrumentCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import execution.engine.service.BarService;
import execution.engine.service.RedisService;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarServiceImpl implements BarService {

    private final InstrumentCache instrumentCache;
    private final RedisService redisService;

    @Override
    public final void onNewBarEvent(String instrument, String timestamp) {
        Bar bar = redisService.getBar(instrument, timestamp);
        instrumentCache.updateInstrument(instrument, bar);
    }
}
