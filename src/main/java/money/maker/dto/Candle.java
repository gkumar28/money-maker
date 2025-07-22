package money.maker.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Candle {

    public Candle(String instrumentToken) {
        this.instrumentToken = instrumentToken;
        this.startTime = Instant.now().toEpochMilli();
        this.endTime = Instant.now().toEpochMilli();
    }

    private String instrumentToken;
    private long startTime;
    private long endTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    public void applyTick(Tick tick) {
        double price = tick.getPrice();
        if (open == 0) open = price;
        if (price > high || high == 0) high = price;
        if (price < low || low == 0) low = price;
        close = price;
        volume += tick.getVolume();
        endTime = Math.max(endTime, tick.getTimestamp());
    }

}
