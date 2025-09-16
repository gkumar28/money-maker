package strategy.engine.service.impl;

import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import strategy.engine.feignclient.BrokerIntegratorClient;
import strategy.engine.service.MarketDataService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("dev")
public class MarketDataServiceImpl implements MarketDataService {

    private final BrokerIntegratorClient brokerIntegratorClient;

    @Override
    public Path loadRawData(String instrument, String exchange, LocalDateTime fromDate, LocalDateTime toDate, String interval) {
        try {
            String filename = String.format("%s_%s_%s_%s.csv", instrument, exchange, fromDate, toDate);
            Path outputDir = Paths.get("files", "output");
            Files.createDirectories(outputDir);
            Path outputFile = outputDir.resolve(filename);

            if (Files.exists(outputFile)) {
                return outputFile;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            Response brokerResponse = brokerIntegratorClient.getHistoricalDataCsv(instrument, exchange, formatter.format(fromDate), formatter.format(toDate), interval);
            InputStream responseBody = brokerResponse.body().asInputStream();

            // Save stream directly to file
            Files.copy(responseBody, outputFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Raw data saved: {}", outputFile.toAbsolutePath());

            return outputFile.toAbsolutePath();
        } catch (IOException e) {
            log.error("Raw data load failed", e);
        }

        return null;
    }

    @Override
    public Bar historicalCsvStringToBar(String csvString, Duration duration) {
        String[] cols = csvString.split(",");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        ZonedDateTime endTime = ZonedDateTime.parse(cols[0], formatter);
        double open = Double.parseDouble(cols[1]);
        double high = Double.parseDouble(cols[2]);
        double low = Double.parseDouble(cols[3]);
        double close = Double.parseDouble(cols[4]);
        double volume = Double.parseDouble(cols[5]);
        return new BaseBar(
            duration,
            endTime,
            open,
            high,
            low,
            close,
            volume
        );
    }
}
