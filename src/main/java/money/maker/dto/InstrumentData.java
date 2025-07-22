package money.maker.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayDeque;
import java.util.Deque;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class InstrumentData {

    String instrumentToken;
    int n;
    Deque<Candle> lastNCandles;
    Double previousEma;
    int longSmaPeriod;
    int rsiPeriod;
    int shortEmaPeriod;

    double sma;
    double ema;
    double rsi;

    public InstrumentData(String instrument, int longSmaPeriod, int shortEmaPeriod, int rsiPeriod) {
        this.instrumentToken = instrument;
        this.longSmaPeriod = longSmaPeriod;
        this.shortEmaPeriod = shortEmaPeriod;
        this.rsiPeriod = rsiPeriod;
        this.n = Math.max(longSmaPeriod, rsiPeriod + 1);
        this.lastNCandles = new ArrayDeque<>(n);
        this.previousEma = null;
    }

    public void addCandle(Candle candle) {
        if (lastNCandles.size() == n) {
            lastNCandles.pollFirst();
        }
        lastNCandles.addLast(candle);
    }

    public Double calculateSMA() {
        if (lastNCandles.size() < longSmaPeriod) return Double.NaN;

        return lastNCandles.stream()
            .skip(lastNCandles.size() - longSmaPeriod)
            .mapToDouble(Candle::getClose)
            .average()
            .orElse(Double.NaN);
    }

    // Calculate EMA (incremental)
    public Double calculateEMA() {
        if (lastNCandles.isEmpty()) return null;

        double closePrice = lastNCandles.getLast().getClose();
        int period = shortEmaPeriod;
        double alpha = 2.0 / (period + 1);

        if (previousEma == null) {
            // initialize EMA with SMA of shortEmaPeriod
            if (lastNCandles.size() < shortEmaPeriod) return Double.NaN; // not enough data

            previousEma = lastNCandles.stream()
                .skip(lastNCandles.size() - shortEmaPeriod)
                .mapToDouble(Candle::getClose)
                .average()
                .orElse(Double.NaN);
        } else {
            previousEma = (closePrice * alpha) + (previousEma * (1 - alpha));
        }
        return previousEma;
    }

    // Calculate RSI
    public Double calculateRSI() {
        if (lastNCandles.size() < rsiPeriod + 1) return Double.NaN;

        double gainSum = 0;
        double lossSum = 0;

        Candle prev = null;
        // Only consider last rsiPeriod + 1 candles for RSI
        Candle[] lastCandlesArray = lastNCandles.toArray(new Candle[0]);
        int startIdx = lastCandlesArray.length - (rsiPeriod + 1);

        for (int i = startIdx; i < lastCandlesArray.length; i++) {
            Candle current = lastCandlesArray[i];
            if (prev != null) {
                double change = current.getClose() - prev.getClose();
                if (change > 0) gainSum += change;
                else lossSum += -change;
            }
            prev = current;
        }

        double avgGain = gainSum / rsiPeriod;
        double avgLoss = lossSum / rsiPeriod;

        if (avgLoss == 0) return 100.0;

        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));

    }

    public void updateIndicators(Candle candle) {
        addCandle(candle);
        sma = calculateSMA();
        ema = calculateEMA();
        rsi = calculateRSI();
    }
    
}
