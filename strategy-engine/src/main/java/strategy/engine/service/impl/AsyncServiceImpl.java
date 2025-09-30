package strategy.engine.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import strategy.engine.service.AsyncService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Service
@Slf4j
public class AsyncServiceImpl implements AsyncService {

    private final Executor executor;

    public AsyncServiceImpl(@Qualifier("taskExecutor")ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public <T> Future<T> run(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
}
