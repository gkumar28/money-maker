package execution.engine.entity;

import execution.engine.constant.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "positions")
public class Position {

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

    @Enumerated(EnumType.STRING)
    @Column
    private OrderStatus status;

}

