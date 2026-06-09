package sre.engine.strategy.strategy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import sre.engine.strategy.schemaobject.strategy.tree.RuleDefinition;

@Data
@With
@Builder
public class StrategyDefinition {

    private final String name;
    private final String interval;
    private final int delay;
    private final RuleDefinition entry;
    private final RuleDefinition exit;
    private final RuleDefinition expand;
    private final RuleDefinition trim;

    @JsonCreator
    public StrategyDefinition(
        @JsonProperty("name") String name,
        @JsonProperty("interval") String interval,
        @JsonProperty("delay") int delay,
        @JsonProperty("entry") RuleDefinition entry,
        @JsonProperty("exit") RuleDefinition exit,
        @JsonProperty("expand") RuleDefinition expand,
        @JsonProperty("trim") RuleDefinition trim
    ) {
        this.name = name;
        this.interval = interval;
        this.delay = delay;
        this.entry = entry;
        this.exit = exit;
        this.expand = expand;
        this.trim = trim;
    }
}
