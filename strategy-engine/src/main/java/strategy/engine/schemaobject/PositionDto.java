package strategy.engine.schemaobject;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.util.StrategyEngineUtils;

@Data
@RequiredArgsConstructor
public class PositionDto {

    private TradeDto entry;
    private TradeDto exit;
    private final TradeDirection startingType;

    public PositionDto(TradeDto entry) {
        this.entry = entry;
        this.startingType = entry.getDirection();
    }

    public int getOpenQuantity() {
        if (null == entry) {
             return 0;
        }

        if (null == exit) {
            return entry.getQuantity();
        }

        return entry.getQuantity() - exit.getQuantity();
    }

    public boolean isOpen() {
        if (null == entry) {
            return false;
        }

        if (null == exit) {
            return true;
        }

        return entry.getQuantity() - exit.getQuantity() > 0;
    }

    public TradeDto updateExitWith(TradeDto newPartialExit) {
        if (newPartialExit == null) {
            throw new IllegalArgumentException("New exit trade must not be null");
        }

        if (this.exit == null) {
            this.exit = newPartialExit;
            return this.exit;
        }

        this.exit = StrategyEngineUtils.mergeTrades(this.exit, newPartialExit);
        return this.exit;
    }
}
