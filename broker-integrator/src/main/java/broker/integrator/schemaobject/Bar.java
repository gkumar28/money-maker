package broker.integrator.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Bar {

    private String timeStamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private long oi;
}
