package strategy.engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import strategy.engine.serializer.BigDecimalSerializer;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Configuration
public class JacksonConfig {

    @Bean("jsonObjectMapper")
    @Primary
    public ObjectMapper jsonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Optional: Add other common configs
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, new BigDecimalSerializer(2, RoundingMode.HALF_UP));
        mapper.registerModule(module);

        return mapper;
    }

    @Bean("yamlObjectMapper")
    public ObjectMapper yamlObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // Optional: Add other common configs
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, new BigDecimalSerializer(2, RoundingMode.HALF_UP));
        mapper.registerModule(module);

        return mapper;
    }
}
