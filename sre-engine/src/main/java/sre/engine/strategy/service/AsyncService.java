package sre.engine.strategy.service;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface AsyncService {

    <T> Future<T> run(Supplier<T> supplier);
}
