package broker.integrator.controller;

import broker.integrator.component.ZerodhaClient;
import broker.integrator.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import static broker.integrator.constant.ApplicationConstant.ZERODHA;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ZerodhaApiController implements ZerodhaApi {

    private final RedisService redisService;
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
}
