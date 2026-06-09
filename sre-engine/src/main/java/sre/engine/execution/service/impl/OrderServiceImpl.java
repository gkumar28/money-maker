package sre.engine.execution.service.impl;

import sre.engine.execution.schemaobject.Tick;
import sre.engine.execution.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Override
    public void handle(Tick tick) {
    }
}
