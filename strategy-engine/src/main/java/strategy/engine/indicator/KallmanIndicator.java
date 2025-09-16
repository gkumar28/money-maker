package strategy.engine.indicator;

import org.ejml.simple.SimpleMatrix;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.HashMap;
import java.util.Map;

public class KallmanIndicator extends CachedIndicator<Num> {

    private final Map<Integer, KallmanState> stateCache;
    private final Indicator<Num> priceIndicator;
    private final Num q; // Process noise variance
    private final int rWindow; // Window size for measurement noise estimation

    // Process noise covariance Q (2x2)
    private final SimpleMatrix noiseCovariance;

    // State Transition matrix A
    private final SimpleMatrix transition = new SimpleMatrix(new double[][] {
        {1, 1},
        {0, 1}
    });
    // Measurement matrix H (maps state to measurement)
    private final SimpleMatrix measurement = new SimpleMatrix(new double[][] {{1, 0}});

    public KallmanIndicator(Indicator<Num> priceIndicator, double qValue, int rWindow) {
        super(priceIndicator.getBarSeries());
        this.stateCache = new HashMap<>();
        this.priceIndicator = priceIndicator;
        this.q = priceIndicator.numOf(qValue);
        this.rWindow = rWindow;
        noiseCovariance = new SimpleMatrix(new double[][]{
            {qValue, 0},
            {0, qValue}
        });
    }

    @Override
    protected Num calculate(int index) {
        if (index == 0) {
            double initialPrice = priceIndicator.getValue(0).doubleValue();
            KallmanState initial = new KallmanState(
                new SimpleMatrix(new double[][]{{initialPrice}, {0}}),
                SimpleMatrix.identity(2)
            );
            stateCache.put(0, initial);
            return priceIndicator.numOf(initialPrice);
        }

        KallmanState prev = stateCache.get(index - 1);
        if (prev == null) {
            calculate(index - 1);
            prev = stateCache.get(index - 1);
        }

        // Prediction
        SimpleMatrix xPred = transition.mult(prev.x);
        SimpleMatrix pPred = transition.mult(prev.p).mult(transition.transpose()).plus(noiseCovariance);

        // deviation from prediction
        double measurementVal = priceIndicator.getValue(index).doubleValue();
        SimpleMatrix z = new SimpleMatrix(new double[][]{{measurementVal}});
        SimpleMatrix y = z.minus(measurement.mult(xPred));

        // Correction of predicted value basis current observation
        double r = estimateMeasurementNoise(index);
        SimpleMatrix R = new SimpleMatrix(new double[][]{{r}});
        SimpleMatrix S = measurement.mult(pPred).mult(measurement.transpose()).plus(R);
        SimpleMatrix K = pPred.mult(measurement.transpose()).mult(S.invert());

        // filtered estimate (post current observation)
        SimpleMatrix xNew = xPred.plus(K.mult(y));
        SimpleMatrix I = SimpleMatrix.identity(2);
        SimpleMatrix pNew = (I.minus(K.mult(measurement))).mult(pPred);

        KallmanState current = new KallmanState(xNew, pNew);
        stateCache.put(index, current);

        // z-score: prediction / std deviation
        return priceIndicator.numOf(y.get(0, 0) / Math.sqrt(S.get(0, 0)));
    }

    private double estimateMeasurementNoise(int index) {
        int start = Math.max(0, index - rWindow + 1);
        if (index - start + 1 < 2) {
            return 0.01;
        }
        double sum = 0;
        double sumSq = 0;
        int count = index - start + 1;
        for (int i = start; i <= index; i++) {
            double val = priceIndicator.getValue(i).doubleValue();
            sum += val;
            sumSq += val * val;
        }
        double mean = sum / count;
        double variance = (sumSq - (mean * mean) * count) / (count - 1);
        return Math.max(variance, 1e-4);
    }

    @Override
    public synchronized Num getValue(int index) {
        return super.getValue(index);
    }

    @Override
    public int getUnstableBars() {
        return 0;
    }

    @Override
    public BarSeries getBarSeries() {
        return priceIndicator.getBarSeries();
    }

    @Override
    public Num numOf(Number number) {
        return priceIndicator.numOf(number);
    }

    private record KallmanState(SimpleMatrix x, SimpleMatrix p) { }
}
