package broker.integrator.controller;

import broker.integrator.schemaobject.Bar;
import com.zerodhatech.models.HistoricalData;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/historical-data")
    ResponseEntity<List<Bar>> getHistoricalData(@RequestParam("instrument") String instrument,
                                                @RequestParam("from") LocalDateTime from,
                                                @RequestParam("to") LocalDateTime to,
                                                @RequestParam("interval") String interval);

    @GetMapping("historical-data/csv")
    void getHistoricalDataCsv(HttpServletResponse response,
                              @RequestParam("instrument") String instrument,
                              @RequestParam("from") LocalDateTime from,
                              @RequestParam("to") LocalDateTime to,
                              @RequestParam("interval") String interval);
}
