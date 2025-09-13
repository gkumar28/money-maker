package broker.integrator.service;

public interface RedisService {

    void raiseConnectedEvent(String broker, String accessToken);
}
