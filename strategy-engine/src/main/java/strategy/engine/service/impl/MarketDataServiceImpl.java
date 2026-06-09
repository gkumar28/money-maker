package strategy.engine.service.impl;

import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.num.NumFactory;
import strategy.engine.feignclient.BrokerIntegratorClient;
import strategy.engine.service.MarketDataService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("dev")
public class MarketDataServiceImpl implements MarketDataService {

    private final BrokerIntegratorClient brokerIntegratorClient;

    @Override
    public Path loadRawData(String instrument, String exchange, LocalDateTime fromDate, LocalDateTime toDate, String interval) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String filename = String.format("%s_%s_%s_%s_%s.csv", instrument, exchange, interval, formatter.format(fromDate), formatter.format(toDate));
            Path dataDir = Paths.get("files", "input");
            Files.createDirectories(dataDir);
            Path dataFile = dataDir.resolve(filename);

            if (Files.exists(dataFile)) {
                return dataFile;
            }

            Response brokerResponse = brokerIntegratorClient.getHistoricalDataCsv(instrument, exchange, formatter.format(fromDate), formatter.format(toDate), interval);
            InputStream responseBody = brokerResponse.body().asInputStream();

            // Save stream directly to file
            Files.copy(responseBody, dataFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Raw data saved: {}", dataFile.toAbsolutePath());

            return dataFile.toAbsolutePath();
        } catch (IOException e) {
            log.error("Raw data load failed", e);
        }

        return null;
    }

}
