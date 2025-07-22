package money.maker.component;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@RequiredArgsConstructor
public class RiskManager {

    private final List<String> instruments = List.of("RELIANCE");
}
