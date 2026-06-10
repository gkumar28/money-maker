package sre.engine.strategy.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import common.lib.schemaobjects.Portfolio;

@Data
@Component
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioContainer {

    private Portfolio portfolio;


}
