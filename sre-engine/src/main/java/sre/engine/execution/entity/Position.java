package sre.engine.execution.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
public class Position {

    public Position(String instrument) {
        this.setInstrument(instrument);
        this.setQuantity(BigDecimal.ZERO);
        this.setAverageEntryPrice(BigDecimal.ZERO);
        this.setRealizedProfit(BigDecimal.ZERO);
        this.setOpenedAt(ZonedDateTime.now(ZoneId.of("UTC")).toOffsetDateTime());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String instrument;

    @Column(precision = 19, scale = 5)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 5)
    private BigDecimal averageEntryPrice;

    @Column(precision = 19, scale = 5)
    private BigDecimal realizedProfit;

    @Column
    private OffsetDateTime openedAt;

    @Column
    private OffsetDateTime closedAt;

    public boolean isOpen() {
        return null == this.getClosedAt();
    }
}

