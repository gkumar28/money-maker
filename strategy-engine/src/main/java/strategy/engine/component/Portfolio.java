package strategy.engine.component;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Data
@Component
@RequiredArgsConstructor
public class Portfolio {

    private final Set<String> instruments = Set.of("RELIANCE");
}
