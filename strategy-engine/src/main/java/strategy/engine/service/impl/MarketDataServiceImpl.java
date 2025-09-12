package strategy.engine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.service.MarketDataService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("dev")
public class MarketDataServiceImpl implements MarketDataService {

    private final RestTemplate restTemplate;

    @Override
    public BarSeries loadHistoricalData(String instrument, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return loadHistoricalData(instrument, dateTimeFormatter.format(fromDate), dateTimeFormatter.format(toDate));
    }

    @Override
    public BarSeries loadHistoricalData(String instrument, String fromDate, String toDate) {
        List<String[]> rangeChunks = chunkDateRange(fromDate, toDate);
        BarSeries barSeries = new BaseBarSeries(instrument);
        for (String[] chunk: rangeChunks) {
            List<Map<String, Object>> data = getData(instrument, chunk[0], chunk[1]);
            for (Map<String, Object> json: data) {
                barSeries.addBar(parseToBar(json));
            }
        }

        barSeries.setMaximumBarCount(barSeries.getBarCount());
        return barSeries;
    }

    @Override
    public List<Map<String, Object>> loadRawData(String instrument, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return loadRawData(instrument, dateTimeFormatter.format(fromDate), dateTimeFormatter.format(toDate));
    }

    @Override
    public List<Map<String, Object>> loadRawData(String instrument, String fromDate, String toDate) {
        List<String[]> rangeChunks = chunkDateRange(fromDate, toDate);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] chunk: rangeChunks) {
            List<Map<String, Object>> data = getData(instrument, chunk[0], chunk[1]);
            result.addAll(data);
        }

        return result;
    }

    private static List<String[]> chunkDateRange(String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDate = LocalDate.parse(start, formatter);
        LocalDate endDate = LocalDate.parse(end, formatter);
        List<String[]> chunks = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            LocalDate chunkEnd = startDate.plusDays(49);
            if (chunkEnd.isAfter(endDate)) {
                chunkEnd = endDate;
            }
            chunks.add(new String[]{
                startDate.format(formatter),
                chunkEnd.format(formatter)
            });
            startDate = chunkEnd.plusDays(1);
        }

        return chunks;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getData(String instrument, String fromDate, String toDate) {
        String url = "https://www.nseindia.com/api/historicalOR/generateSecurityWiseHistoricalData?from=%s&to=%s&symbol=%s&type=priceVolumeDeliverable&series=ALL";

        url = String.format(url, fromDate, toDate, instrument);
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Map.class
        );
        Map<String, Object> body = response.getBody();
        return (List<Map<String, Object>>) body.get("data");
    }

    private Bar parseToBar(Map<String, Object> json) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZonedDateTime endTime = ZonedDateTime.from(formatter.parse((String) json.get("CH_TIMESTAMP")));

        // Create the Bar
        return new BaseBar(
            Duration.ofDays(1),
            endTime,
            DecimalNum.valueOf(json.get("CH_OPENING_PRICE").toString()),
            DecimalNum.valueOf(json.get("CH_TRADE_HIGH_PRICE").toString()),
            DecimalNum.valueOf(json.get("CH_TRADE_LOW_PRICE").toString()),
            DecimalNum.valueOf(json.get("CH_CLOSING_PRICE").toString()),
            DecimalNum.valueOf(json.get("CH_TOT_TRADED_QTY").toString()),
            DecimalNum.valueOf(json.get("CH_TOT_TRADED_VAL").toString()),
            Long.parseLong(json.get("CH_TOTAL_TRADES").toString())
        );
    }
}
