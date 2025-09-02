package money.maker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.service.BarService;
import money.maker.service.RedisService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarServiceImpl implements BarService {

    private final RedisService redisService;
    @Override
    public final void onNewBarEvent(String instrument, String timestamp) {

    }
}
