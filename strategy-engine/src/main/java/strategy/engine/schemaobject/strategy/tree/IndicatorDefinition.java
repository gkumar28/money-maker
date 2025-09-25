package strategy.engine.schemaobject.strategy.tree;

import lombok.Data;
import strategy.engine.constant.enums.IndicatorType;

import java.util.List;
import java.util.Map;

@Data
public class IndicatorDefinition {

    private final IndicatorType type;
    private final Map<String, Object> parameters;
    private final List<IndicatorDefinition> inputs;

}
