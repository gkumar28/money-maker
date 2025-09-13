package broker.integrator.schemaobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerContext {

    private String client;
    private String event;
    private String accessToken;
}
