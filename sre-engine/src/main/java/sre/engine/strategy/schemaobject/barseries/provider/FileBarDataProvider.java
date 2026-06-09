package sre.engine.strategy.schemaobject.barseries.provider;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarBuilder;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileBarDataProvider extends BarDataProvider {

    private final long[] offsets;
    private final MappedByteBuffer buffer;

    public FileBarDataProvider(Path path) {
        MappedByteBuffer tmpBuffer = null;
        long[] tmp = {};
        try(FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            List<Long> offsetsList = new ArrayList<>();
            tmpBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            offsetsList.add(0L);
            for (int i = 0; i < channel.size(); i++) {
                if (tmpBuffer.get(i) == '\n') {
                    offsetsList.add((long) i + 1);
                }
            }
            tmp = offsetsList.stream().mapToLong(Long::longValue).toArray();
        } catch (Exception e) {
            log.error("Failed to instantiate class", e);
        }

        offsets = tmp;
        buffer = tmpBuffer;
    }

    @Override
    public List<Bar> fetchBars(int startIndex, int count, BarBuilder barBuilder) {
        List<Bar> result = new ArrayList<>();
        for(int i=startIndex;i<startIndex + count;i++) {
            String row = getRow(i);
            if (null != row) {
                result.add(historicalCsvStringToBar(row, barBuilder));
            }
        }

        return result;
    }

    @Override
    public int getDataSize() { return offsets.length; }

    private String getRow(int rowIndex) {
        if (rowIndex >= offsets.length) { return null; }
        int start = (int) offsets[rowIndex];
        int end = start;

        while (end < buffer.limit()
                && buffer.get(end) != '\n'
                && buffer.get(end) != '\r') {
            end++;
        }

        byte[] bytes = new byte[end - start];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buffer.get(start + i);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Bar historicalCsvStringToBar(String csvString, BarBuilder barBuilder) {
        String[] parts = csvString.split(",");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        ZonedDateTime endTime = ZonedDateTime.parse(parts[0], formatter);

        return barBuilder
                .endTime(Instant.from(endTime))
                .openPrice(parts[1])
                .highPrice(parts[2])
                .lowPrice(parts[3])
                .closePrice(parts[4])
                .volume(parts[5])
                .build();
    }
}
