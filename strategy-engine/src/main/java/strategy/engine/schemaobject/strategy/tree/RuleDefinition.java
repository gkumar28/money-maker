package strategy.engine.schemaobject.strategy.tree;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import strategy.engine.constant.enums.RuleType;

@Data
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "_deftype"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = LogicalRuleDefinition.class, name = "LOGICAL"),
    @JsonSubTypes.Type(value = LeafRuleDefinition.class, name = "LEAF")
})
public abstract class RuleDefinition {

    protected final RuleType ruleType;
}
