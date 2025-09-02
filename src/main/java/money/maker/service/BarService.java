package money.maker.service;

public interface BarService {

    void onNewBarEvent(String instrument, String timestamp);
}
