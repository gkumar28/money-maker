package money.maker.route;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketDataRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:websocket-data")
            .process("tickAggregatorProcessor");
    }
}
