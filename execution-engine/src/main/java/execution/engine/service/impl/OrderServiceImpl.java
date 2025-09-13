package execution.engine.service.impl;

import execution.engine.schemaobject.Tick;
import execution.engine.service.OrderService;
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
