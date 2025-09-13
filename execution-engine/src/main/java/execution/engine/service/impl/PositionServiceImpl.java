package execution.engine.service.impl;

import execution.engine.constant.enums.TradeSide;
import execution.engine.entity.Fill;
import execution.engine.entity.Position;
import execution.engine.repository.PositionRepository;
import execution.engine.schemaobject.SignalState;
import execution.engine.service.OrderService;
import execution.engine.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final OrderService orderService;

    @Override
    public Position getOrCreateOpenPosition(String instrument) {
        return positionRepository
            .findByInstrumentAndClosedAtIsNull(instrument)
            .orElseGet(() -> new Position(instrument));
    }

    @Override
    public void handleSignal(String instrument, SignalState signal) {
        Position position = getOrCreateOpenPosition(instrument);

        switch (signal.getSignal()) {
            case ENTRY -> {
                BigDecimal quantityToAdd = calculateEntryQuantity(position, signal.getPrice());
                //orderService.placeMarketOrder(instrument, quantityToAdd, TradeSide.BUY);
            }

            case EXIT -> {
                if (!position.isOpen() || position.getQuantity().compareTo(BigDecimal.ZERO) <= 0) return;
                //orderService.placeMarketOrder(instrument, position.getQuantity(), TradeSide.SELL);
            }

            case HOLD -> {
                // Optional: check for trimming or rebalancing logic
            }
        }
    }

    @Override
    public void updatePositionWithFill(String instrument, Fill fill) {
        Position position = getOrCreateOpenPosition(fill.getOrder().getInstrument());

        if (fill.getOrder().getSide() == TradeSide.BUY) {
            expandPosition(instrument, fill.getQuantity(), fill.getPrice());
        } else {
            trimPosition(instrument, fill.getQuantity(), fill.getPrice());
        }

        positionRepository.save(position);
    }

    @Override
    public void expandPosition(String instrument, BigDecimal fillQty, BigDecimal fillPrice) {
        Position position = getOrCreateOpenPosition(instrument);

        BigDecimal oldQty = position.getQuantity();
        BigDecimal totalCost = position.getAverageEntryPrice().multiply(oldQty)
            .add(fillPrice.multiply(fillQty));
        BigDecimal newQty = oldQty.add(fillQty);

        position.setQuantity(newQty);
        position.setAverageEntryPrice(totalCost.divide(newQty, 5, HALF_UP));

        positionRepository.save(position);
    }

    @Override
    public void trimPosition(String instrument, BigDecimal exitQty, BigDecimal exitPrice) {
        Position position = getOrCreateOpenPosition(instrument);

        if (exitQty.compareTo(position.getQuantity()) > 0) {
            throw new IllegalArgumentException("Exit quantity exceeds position quantity.");
        }

        BigDecimal pnl = exitPrice.subtract(position.getAverageEntryPrice())
            .multiply(exitQty);
        BigDecimal newQty = position.getQuantity().subtract(exitQty);

        position.setRealizedProfit(position.getRealizedProfit().add(pnl));
        position.setQuantity(newQty);

        if (newQty.compareTo(BigDecimal.ZERO) <= 0.01) {
            position.setClosedAt(ZonedDateTime.now(ZoneId.of("UTC")).toOffsetDateTime());
        }

        positionRepository.save(position);
    }

    private BigDecimal calculateEntryQuantity(Position position, BigDecimal price) {
        // Placeholder: Fixed size for now
        return new BigDecimal("1.0");
    }

}
