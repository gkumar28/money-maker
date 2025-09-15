package broker.integrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instruments")
@Data
@NoArgsConstructor
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_token", nullable = false)
    private Long instrumentToken;

    @Column(name = "exchange_token", nullable = false)
    private Long exchangeToken;

    @Column(name = "trading_symbol", nullable = false)
    private String tradingSymbol;

    @Column(name = "segment", nullable = false)
    private String segment;

    @Column(name = "exchange", nullable = false)
    private String exchange;

}
