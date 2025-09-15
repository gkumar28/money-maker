package broker.integrator.controller;

import broker.integrator.component.ZerodhaClient;
import broker.integrator.schemaobject.Bar;
import broker.integrator.service.MarketDataService;
import broker.integrator.service.RedisService;
import com.zerodhatech.models.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static broker.integrator.constant.ApplicationConstant.ZERODHA;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ZerodhaApiController implements ZerodhaApi {

    private final RedisService redisService;
    private final MarketDataService marketDataService;
    private final ZerodhaClient zerodhaClient;

    @Override
    public String login() {
        return "redirect:" + zerodhaClient.getLoginUrl();
    }

    @Override
    public ResponseEntity<String> redirect(String requestToken) {
        String accessToken = zerodhaClient.getAccessToken(requestToken);
        redisService.raiseConnectedEvent(ZERODHA, accessToken);
        return ResponseEntity.ok("login successful");
    }

    @Override
    public ResponseEntity<User> getUser() {
        return ResponseEntity.ok(zerodhaClient.getUserModel());
    }

    @Override
    public ResponseEntity<Void> setUser(User user) {
        zerodhaClient.setUserModel(user);
        zerodhaClient.setAuth(user.accessToken, user.publicToken);
        return ResponseEntity.noContent().build();
    }

    @Override
    public void getHistoricalDataCsv(HttpServletResponse response, String instrument, String exchange, LocalDateTime from, LocalDateTime to, String interval) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"data.csv\"");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            LocalDateTime start = from;

            writer.println("Timestamp,Open,High,Low,Close,Volume,Open Interest");
            while (!start.isAfter(to)) {
                LocalDateTime end = start.plus(getMaxEndTime(interval));

                if (end.isAfter(to)) {
                    end = to;
                }

                List<Bar> data = marketDataService.getHistoricalData(instrument, exchange, start, end, interval);
                for (Bar bar: data) {
                    writer.println(convertToCsv(bar));
                }

                writer.flush();
                start = end.plusSeconds(1);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if (writer != null) {
                writer.println("Error generating CSV: " + e.getMessage());
                writer.flush();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private Duration getMaxEndTime(String interval) {
        return switch (interval) {
            case "minute" -> Duration.ofDays(60);
            case "3minute", "5minute", "60minute" -> Duration.ofDays(100);
            default -> Duration.ofDays(365);
        };
    }

    private String convertToCsv(Bar bar) {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%d,%d",
                bar.getTimeStamp() != null ? bar.getTimeStamp() : "",
                bar.getOpen(),
                bar.getHigh(),
                bar.getLow(),
                bar.getClose(),
                bar.getVolume(),
                bar.getOi()
            );
    }


}
