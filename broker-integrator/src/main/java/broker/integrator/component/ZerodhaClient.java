package broker.integrator.component;

import broker.integrator.schemaobject.Bar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class ZerodhaClient {

    @Value("${broker.kite.api-key}")
    private String apiKey;

    @Value("${broker.kite.api-secret}")
    private String apiSecret;

    private final ObjectMapper objectMapper;

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

    public void login(String requestToken) {
        try {
            User user = kiteConnect.generateSession(requestToken, apiSecret);
            this.userModel = user;
            setAuth(user.accessToken, user.publicToken);
        } catch (Exception | KiteException exception) {
            log.error("User login failed", exception);
        }
    }

    public void setAuth(String accessToken, String publicToken) {
        kiteConnect.setAccessToken(accessToken);
        kiteConnect.setPublicToken(publicToken);
    }

    public String getAccessToken(String requestToken) {
        login(requestToken);
        return userModel.accessToken;
    }

    public List<Bar> getHistoricalData(String instrument, LocalDateTime from, LocalDateTime to, String interval, boolean continuous, boolean openInterest) {
        try {
            Date fromDate = Date.from(from.atZone(ZoneId.of("Asia/Kolkata")).toInstant());
            Date toDate = Date.from(to.atZone(ZoneId.of("Asia/Kolkata")).toInstant());
            HistoricalData data = kiteConnect.getHistoricalData(fromDate, toDate, instrument, interval, continuous, openInterest);
            return data.dataArrayList.stream().map(ohlcv -> objectMapper.convertValue(ohlcv, Bar.class)).toList();
        } catch (IOException | KiteException exception) {
            log.error("historical-data fetch failed", exception);
        }
        return List.of();
    }
}
