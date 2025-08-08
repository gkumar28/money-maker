package money.maker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.cache.InstrumentCache;
import money.maker.config.external.BarConfiguration;
import money.maker.service.RedisService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static money.maker.constant.ApplicationConstants.BAR;
import static money.maker.constant.ApplicationConstants.DATA;
import static money.maker.constant.ApplicationConstants.TIMESTAMP;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final BarConfiguration barConfiguration;
    private final InstrumentCache instrumentCache;
    private final StringRedisTemplate redisTemplate;

    @Override
    public BarSeries initInstrument(String instrument, int barCount) {
        Set<String> lastNBars = redisTemplate.opsForZSet()
            .reverseRange(getKey(BAR, TIMESTAMP, instrument), 0, barCount - 1L);
        BarSeries barSeries = new BaseBarSeries();

        if (ObjectUtils.isNotEmpty(lastNBars)) {
            List<String> lastNBarsSort = new ArrayList<>(lastNBars.stream().toList());
            List<Object> csvData = redisTemplate.opsForHash().multiGet(instrument, new ArrayList<>(lastNBars));

            for (int i=barCount-1;i>=0;i--) {
                String timestamp = lastNBarsSort.get(i);
                Bar bar = fromCsvString(timestamp, csvData.get(i).toString());
                barSeries.addBar(bar);
            }
        }

        return barSeries;
    }

    @Override
    public void updateInstrument(String instrument, Bar bar) {

        String dataKey = getKey(BAR, DATA, instrument);
        String timestamp = bar.getEndTime().toString();
        String value = toCsvString(bar);
        redisTemplate.opsForHash().put(dataKey, timestamp, value);

        String timestampKey = getKey(BAR, TIMESTAMP, instrument);
        redisTemplate.opsForZSet().add(timestampKey, timestamp, Instant.now().toEpochMilli());

        String channel = String.format("new.bar.%s", instrument);
        redisTemplate.convertAndSend(channel, timestamp);
    }

    @Override
    @Async("taskExecutor")
    public void onNewBarUpdate(String instrument, String timestamp) {
        String value = (String) redisTemplate.opsForHash().get(getKey(BAR, DATA, instrument), timestamp);
        if (null != value) {
            instrumentCache.updateInstrument(instrument, fromCsvString(timestamp, value));
        }
    }

    private String toCsvString(Bar bar) {
        return String.format("%.5f,%.5f,%.5f,%.5f,%.2f,%.2f",
            bar.getOpenPrice().doubleValue(),
            bar.getHighPrice().doubleValue(),
            bar.getLowPrice().doubleValue(),
            bar.getClosePrice().doubleValue(),
            bar.getVolume().doubleValue(),
            bar.getAmount().doubleValue()
        );
    }

    private Bar fromCsvString(String timestamp, String csv) {
        String[] parts = csv.split(",");
        return new BaseBar(
            Duration.ofSeconds(barConfiguration.getTimeFrame()),
            ZonedDateTime.parse(timestamp),
            DecimalNum.valueOf(parts[0]), // open
            DecimalNum.valueOf(parts[1]), // high
            DecimalNum.valueOf(parts[2]), // low
            DecimalNum.valueOf(parts[3]), // close
            DecimalNum.valueOf(parts[4]),  // volume
            DecimalNum.valueOf(parts[5]) // amount
        );
    }

    private String getKey(String keySpace, String subKeySpace, String instrument) {
        return String.join(".", keySpace, subKeySpace, instrument);
    }
}
