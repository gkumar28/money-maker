package data.emitter.service;

public interface RedisService {

    void raiseTickEvent(String instrument, String message);
}
