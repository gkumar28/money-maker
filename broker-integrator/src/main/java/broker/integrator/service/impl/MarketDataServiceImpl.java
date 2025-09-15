package broker.integrator.service.impl;

import broker.integrator.component.ZerodhaClient;
import broker.integrator.entity.Instrument;
import broker.integrator.repository.InstrumentRepository;
import broker.integrator.schemaobject.Bar;
import broker.integrator.schemaobject.InstrumentDto;
import broker.integrator.service.MarketDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataServiceImpl implements MarketDataService {

    private final InstrumentRepository instrumentRepository;
    private final ObjectMapper objectMapper;
    private final ZerodhaClient zerodhaClient;

    @Override
    public List<Bar> getHistoricalData(String instrument, String exchange, LocalDateTime from, LocalDateTime to, String interval) {
        long instrumentToken = getInstrumentToken(instrument, exchange);
        return zerodhaClient.getHistoricalData(String.valueOf(instrumentToken), from, to, interval, false, true);
    }

    @Override
    public void updateInstrumentMasterList() {
        instrumentRepository.deleteAll();
        List<InstrumentDto> instrumentDtos = zerodhaClient.getInstrumentList();
        List<Instrument> instrumentEntities = instrumentDtos.stream()
            .map(instrumentDto -> objectMapper.convertValue(instrumentDto, Instrument.class))
            .toList();
        instrumentRepository.saveAll(instrumentEntities);
    }

    @Override
    public long getInstrumentToken(String tradingSymbol, String exchange) {
        if (instrumentRepository.count() == 0) {
            updateInstrumentMasterList();
        }

        Instrument instrument = instrumentRepository.findByTradingSymbolAndExchange(tradingSymbol, exchange).orElse(new Instrument());
        return instrument.getInstrumentToken();
    }

    private long estimatePoints(LocalDateTime from, LocalDateTime to, Duration interval) {
        Duration totalDuration = Duration.between(from, to);
        long points = totalDuration.toMillis() / interval.toMillis();
        return points + 1;
    }

    private Duration getIntervalDuration(String interval) {
        return switch (interval.toLowerCase().trim()) {
            case "minute", "1minute" -> Duration.ofMinutes(1);
            case "5minute" -> Duration.ofMinutes(5);
            case "60minute", "1hour" -> Duration.ofHours(1);
            case "1day", "day" -> Duration.ofDays(1);
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval);
        };
    }
}
