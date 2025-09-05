package execution.engine.entity;

import execution.engine.constant.enums.OrderStatus;
import execution.engine.constant.enums.OrderType;
import execution.engine.constant.enums.TradeSide;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String instrument;

    @Enumerated(EnumType.STRING)
    @Column
    private TradeSide side;

    @Enumerated(EnumType.STRING)
    @Column
    private OrderType type;

    @Column(precision = 19, scale = 5)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 5)
    private BigDecimal expectedPrice;

    @Enumerated(EnumType.STRING)
    @Column
    private OrderStatus status;

    @Column
    private OffsetDateTime createdAt;
}
