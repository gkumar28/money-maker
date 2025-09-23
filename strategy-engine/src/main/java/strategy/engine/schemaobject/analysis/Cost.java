package strategy.engine.schemaobject.analysis;

import strategy.engine.schemaobject.Position;

import java.math.BigDecimal;

public interface Cost {

    BigDecimal calculate(BigDecimal price, BigDecimal quantity);

}
