package strategy.engine.schemaobject.strategy.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import strategy.engine.constant.enums.IndicatorType;

import java.util.List;
import java.util.Map;

@Data
public class IndicatorDefinition {

    private final IndicatorType indicatorType;
    private final Map<String, Object> parameters;
    private final List<IndicatorDefinition> inputs;

    @JsonCreator
    public IndicatorDefinition(@JsonProperty("indicator_type") IndicatorType type,
                               @JsonProperty("parameters") Map<String, Object> parameters,
                               @JsonProperty("inputs") List<IndicatorDefinition> inputs) {
        this.indicatorType = type;
        this.parameters = parameters;
        this.inputs = inputs;
    }
}
