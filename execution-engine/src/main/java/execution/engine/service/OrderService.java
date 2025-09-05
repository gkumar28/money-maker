package execution.engine.service;

import execution.engine.schemaobject.Tick;

public interface OrderService {

    void handle(Tick tick);
}
