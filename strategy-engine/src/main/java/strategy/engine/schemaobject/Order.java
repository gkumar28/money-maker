package strategy.engine.schemaobject;

import lombok.*;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;
import strategy.engine.schemaobject.signal.Signal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@With
@Builder
public record Order(String id, String instrument, int index, Instant timestamp, Trade.TradeType tradeType, BigDecimal price, BigDecimal capital) {


    public static Order empty(String instrument, Signal signal) {
        return Order.builder()
                .id(UUID.randomUUID().toString())
                .instrument(instrument)
                .index(signal.index())
                .timestamp(signal.timestamp())
                .price(BigDecimal.valueOf(-1))
                .capital(BigDecimal.ZERO)
                .build();
    }

    public static Order empty(String instrument, BigDecimal price) {
        return Order.builder()
                .id(UUID.randomUUID().toString())
                .instrument(instrument)
                .index(-1)
                .timestamp(Instant.now())
                .price(price)
                .capital(BigDecimal.ZERO)
                .build();
    }


}
