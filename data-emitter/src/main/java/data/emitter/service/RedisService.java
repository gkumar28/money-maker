package data.emitter.service;

public interface RedisService {

    void raiseTickEvent(String instrument, String message);
    void updateInstrumentPrice(String instrument, double price);
}
