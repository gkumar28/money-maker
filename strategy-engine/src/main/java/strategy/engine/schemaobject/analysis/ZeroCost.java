package strategy.engine.schemaobject.analysis;

import java.math.BigDecimal;

public class ZeroCost implements Cost {
    @Override
    public BigDecimal calculate(BigDecimal price, BigDecimal quantity) {
        return BigDecimal.ZERO;
    }
}
