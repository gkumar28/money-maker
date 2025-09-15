package broker.integrator.controller;

import broker.integrator.schemaobject.Bar;
import com.zerodhatech.models.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/zerodha")
public interface ZerodhaApi {

    @GetMapping("/login")
    String login();

    @GetMapping("/redirect")
    ResponseEntity<String> redirect(@RequestParam("request_token") String requestToken);

    @GetMapping("/get-user")
    ResponseEntity<User> getUser();

    @PostMapping("/set-user")
    ResponseEntity<Void> setUser(@RequestBody User user);

    @GetMapping("historical-data/csv")
    void getHistoricalDataCsv(HttpServletResponse response,
                              @RequestParam("instrument") String instrument,
                              @RequestParam("exchange") String exchange,
                              @RequestParam("from") LocalDateTime from,
                              @RequestParam("to") LocalDateTime to,
                              @RequestParam("interval") String interval);
}
