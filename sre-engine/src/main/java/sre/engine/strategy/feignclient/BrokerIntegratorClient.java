package sre.engine.strategy.feignclient;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "broker-integrator-client", url="${service.broker-integrator.url}")
public interface BrokerIntegratorClient {

    @GetMapping("/zerodha/historical-data/csv")
    Response getHistoricalDataCsv(
        @RequestParam("instrument") String instrument,
        @RequestParam("exchange") String exchange,
        @RequestParam("from") String from,
        @RequestParam("to") String to,
        @RequestParam("interval") String interval
    );
}
