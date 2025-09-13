package broker.integrator.component;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZerodhaClient {

    @Value("${broker.kite.api-key}")
    private String apiKey;

    @Value("${broker.kite.api-secret}")
    private String apiSecret;

    private KiteConnect kiteConnect;
    private User userModel;

    @PostConstruct
    void init() {
        kiteConnect = new KiteConnect(apiKey);
        userModel = null;
    }

    public boolean isInitialized() {
        return userModel != null;
    }

    public String getLoginUrl() {
        return kiteConnect.getLoginURL();
    }

    public String getAccessToken(String requestToken) {
        try {
            User user = kiteConnect.generateSession(requestToken, apiSecret);
            this.userModel = user;
            return user.accessToken;
        } catch (Exception | KiteException exception) {
            log.error("User login failed", exception);
        }

        return null;
    }
}
