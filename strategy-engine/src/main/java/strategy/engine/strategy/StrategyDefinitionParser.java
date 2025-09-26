package strategy.engine.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Component
@Slf4j
public class StrategyDefinitionParser {

    @Qualifier("yamlObjectMapper")
    private final ObjectMapper yamlObjectMapper;

    @Value("${strategy.definitions.home}")
    private String strategyDefinitionHome;

    public StrategyDefinitionParser(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        this.yamlObjectMapper = yamlObjectMapper;
    }

    public StrategyDefinition read(String fileName) {
        try {
            File strategiesFolder = ResourceUtils.getFile(strategyDefinitionHome);
            Path path = Paths.get(strategiesFolder.toURI()).resolve(fileName);
            String yamlContent = Files.readString(path);
            StrategyDefinition strategyDefinition = yamlObjectMapper.readValue(yamlContent, StrategyDefinition.class);
            log.debug("Successfully parsed strategy definition: {}", fileName);

            return strategyDefinition;
        } catch (IOException e) {
            log.error("Error reading or parsing YAML file: {}", fileName, e);
        }

        return null;
    }

    public StrategyDefinition readAny() {
        try {
            File strategiesFolder = ResourceUtils.getFile(strategyDefinitionHome);
            Optional<Path> strategyDefinitionFilePath = Files.walk(strategiesFolder.toPath())
                .filter(path -> path.toString().endsWith(".yml"))
                .findFirst();

            if (strategyDefinitionFilePath.isPresent()) {
                String yamlContent = Files.readString(strategyDefinitionFilePath.get());
                StrategyDefinition strategyDefinition = yamlObjectMapper.readValue(yamlContent, StrategyDefinition.class);
                log.debug("Parsed strategy definition from classpath: {}", strategyDefinition.getName());
                log.debug("definition: {}", strategyDefinition.toString());
                return strategyDefinition;
            }

        } catch (IOException e) {
            log.error("Error reading or parsing YAML file from home: {}", strategyDefinitionHome,  e);
        }

        return null;
    }
}
