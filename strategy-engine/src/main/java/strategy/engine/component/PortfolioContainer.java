package strategy.engine.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import strategy.engine.schemaobject.Portfolio;

@Data
@Component
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioContainer {

    private Portfolio portfolio;


}
