package sre.engine.strategy.indicator;

import org.ejml.simple.SimpleMatrix;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class KallmanIndicator extends CachedIndicator<Num> {

    private final int stateCacheSizeLimit;
    private final Map<Integer, KallmanState> stateCache;
    private final Indicator<Num> priceIndicator;
    private final Num q; // Process noise variance
    private final int rWindow; // Window size for measurement noise estimation
    private final NumFactory numFactory;

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
        this.numFactory = priceIndicator.getBarSeries().numFactory();
        this.stateCache = new LinkedHashMap<>();
        this.stateCacheSizeLimit = priceIndicator.getBarSeries().getMaximumBarCount();
        this.priceIndicator = priceIndicator;
        this.q = numFactory.numOf(qValue);
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
            return numFactory.numOf(initialPrice);
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

        while(stateCache.size() > stateCacheSizeLimit) {
            Iterator<Integer> it = stateCache.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        // z-score: prediction / std deviation
        return numFactory.numOf(y.get(0, 0) / Math.sqrt(S.get(0, 0)));
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
    public int getCountOfUnstableBars() { return 0; }

    @Override
    public BarSeries getBarSeries() {
        return priceIndicator.getBarSeries();
    }


    private record KallmanState(SimpleMatrix x, SimpleMatrix p) { }
}
