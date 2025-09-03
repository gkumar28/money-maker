package execution.engine.Indicator;

import org.ejml.simple.SimpleMatrix;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class KallmanIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> priceIndicator;
    private final Num q; // Process noise variance
    private final int rWindow; // Window size for measurement noise estimation
    private final int cacheSize;

    // State vector x = [price; velocity]
    private final SimpleMatrix[] xStates;
    // Covariance matrix P (2x2)
    private final SimpleMatrix[] pStates;

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
        this.priceIndicator = priceIndicator;
        this.q = priceIndicator.numOf(qValue);
        this.rWindow = rWindow;
        this.cacheSize = priceIndicator.getBarSeries().getMaximumBarCount();

        noiseCovariance = new SimpleMatrix(new double[][]{
            {qValue, 0},
            {0, qValue}
        });

        xStates = new SimpleMatrix[cacheSize];
        pStates = new SimpleMatrix[cacheSize];
        // Initialize state vector x at first price with zero velocity
        double initialPrice = priceIndicator.getValue(0).doubleValue();
        xStates[0] = new SimpleMatrix(new double[][]{{initialPrice}, {0}});
        pStates[0] = SimpleMatrix.identity(2);
    }

    /**
     * Map logical bar index to cache index by modulo
     */
    private int cacheIndex(int logicalIndex) {
        return logicalIndex % cacheSize;
    }

    @Override
    protected Num calculate(int index) {
        if (index == 0) {
            return getValue(0);
        }

        int prevCachedIndex = cacheIndex(index - 1);
        // Ensure previous states are calculated
        if (xStates[prevCachedIndex] == null) {
            calculate(index - 1);
        }

        // Previous state and covariance
        SimpleMatrix xPrev = xStates[prevCachedIndex];
        SimpleMatrix pPrev = pStates[prevCachedIndex];

        // Prediction step
        SimpleMatrix xPred = transition.mult(xPrev);
        SimpleMatrix pPred = transition.mult(pPrev).mult(transition.transpose()).plus(noiseCovariance);

        // Measurement noise covariance R
        double r = estimateMeasurementNoise(index);
        SimpleMatrix noise = new SimpleMatrix(new double[][] {{r}});

        // Innovation covariance
        SimpleMatrix innovation = measurement.mult(pPred).mult(measurement.transpose()).plus(noise);

        // Kalman Gain
        SimpleMatrix kallman = pPred.mult(measurement.transpose()).mult(innovation.invert());

        // Measurement residual
        double data = priceIndicator.getValue(index).doubleValue();
        SimpleMatrix z = new SimpleMatrix(new double[][] {{data}});
        SimpleMatrix y = z.minus(measurement.mult(xPred));

        // Update step
        SimpleMatrix xNew = xPred.plus(kallman.mult(y));
        SimpleMatrix identity = SimpleMatrix.identity(2);
        SimpleMatrix pNew = (identity.minus(kallman.mult(measurement))).mult(pPred);

        // Cache new states
        xStates[index] = xNew;
        pStates[index] = pNew;

        // Return filtered price (first element of state vector)
        return priceIndicator.numOf(xNew.get(0, 0));
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
        double variance = (sumSq - (mean * mean)) / count;
        return variance < 1e-6 ? 0.001 : variance;
    }

    @Override
    public synchronized Num getValue(int index) {
        return priceIndicator.numOf(xStates[cacheIndex(index)].get(0, 0));
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
        return null;
    }
}
