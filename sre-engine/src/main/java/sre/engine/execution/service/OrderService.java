package sre.engine.execution.service;

import sre.engine.execution.schemaobject.Tick;

public interface OrderService {

    void handle(Tick tick);
}
