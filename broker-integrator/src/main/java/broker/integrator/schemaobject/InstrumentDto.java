package broker.integrator.schemaobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentDto {

    private Long instrumentToken;
    private Long exchangeToken;
    private String tradingSymbol;
    private String segment;
    private String exchange;
}
