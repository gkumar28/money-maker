package sre.engine.strategy.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import sre.engine.strategy.service.TradingRecordManagementService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static sre.engine.strategy.util.StrategyEngineUtils.sanitize;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradingRecordManagementServiceImpl implements TradingRecordManagementService {

    @Override
    public void writeToFile(TradingRecord tradingRecord, String instrument, String exchange, LocalDateTime fromDate, LocalDateTime toDate, String interval) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String filename = String.format("%s_%s_%s_%s_%s.csv",
                instrument,
                exchange,
                interval,
                formatter.format(fromDate),
                formatter.format(toDate));
            Path dataDir = Paths.get("files", "output");
            Files.createDirectories(dataDir);
            Path dataFile = dataDir.resolve(filename);

            List<String> trades = new ArrayList<>(Collections.singleton("Timestamp,Trade type,Quantity,Price,Cost"));
            trades.addAll(tradingRecord.getTrades().stream().map(this::toCsv).toList());
            // Save stream directly to file
            Files.copy(new ByteArrayInputStream(String.join(System.lineSeparator(), trades).getBytes(StandardCharsets.UTF_8)),
                dataFile,
                StandardCopyOption.REPLACE_EXISTING);
            log.info("Trade log saved: {}", dataFile.toAbsolutePath());
        } catch (IOException e) {
            log.error("Raw data load failed", e);
        }
    }

    private String toCsv(Trade trade) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        return String.format("%s,%s,%s,%s,%s",
            formatter.format(trade.getTime()),
            trade.getType(),
            sanitize(trade.getAmount().bigDecimalValue()),
            sanitize(trade.getPricePerAsset().bigDecimalValue()),
            sanitize(trade.getCost().bigDecimalValue()));
    }
}
