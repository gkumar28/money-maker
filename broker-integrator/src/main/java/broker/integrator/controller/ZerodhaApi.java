package broker.integrator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/zerodha")
public interface ZerodhaApi {

    @GetMapping("/login")
    public String login();

    @GetMapping("/redirect")
    public ResponseEntity<String> redirect(@RequestParam("request_token") String requestToken);

}
