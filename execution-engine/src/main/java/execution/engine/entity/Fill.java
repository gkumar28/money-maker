package execution.engine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fills")
public class Fill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Order order;

    @Column(precision = 19, scale = 8)
    private BigDecimal filledQuantity;

    @Column(precision = 19, scale = 8)
    private BigDecimal filledPrice;

    @Column
    private OffsetDateTime filledAt;

    // Getters and setters...
}

