package execution.engine.service;

public interface BarService {

    void onNewBarEvent(String instrument, String timestamp);
}
