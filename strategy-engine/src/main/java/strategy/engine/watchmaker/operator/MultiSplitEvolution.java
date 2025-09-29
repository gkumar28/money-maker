package strategy.engine.watchmaker.operator;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import strategy.engine.strategy.StrategyDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MultiSplitEvolution implements EvolutionaryOperator<StrategyDefinition> {

    private final List<EvolutionaryOperator<StrategyDefinition>> operators;
    private final double[] weights;

    public MultiSplitEvolution(List<EvolutionaryOperator<StrategyDefinition>> operators, double[] weights) {
        this.operators = operators;
        this.weights = weights;
    }
    @Override
    public List<StrategyDefinition> apply(List<StrategyDefinition> selectedCandidates, Random rng) {
        List<StrategyDefinition> selectionClone = new ArrayList<>(selectedCandidates);
        Collections.shuffle(selectionClone, rng);
        int n = selectionClone.size(), k = weights.length;
        int[] sizes = new int[k];
        double[] fractions = new double[k];
        int total = 0;

        for (int i = 0; i < k; i++) {
            double exact = weights[i] * n;
            sizes[i] = (int) exact;
            fractions[i] = exact - sizes[i];
            total += sizes[i];
        }

        int remaining = n - total;
        while (remaining > 0) {
            int max = 0;
            for (int i = 1; i < k; i++) {
                if (fractions[i] > fractions[max]) {
                    max = i;
                }
            }
            sizes[max]++;
            fractions[max] = -1;
            remaining--;
        }

        int idx = 0;
        List<StrategyDefinition> result = new ArrayList<>();
        for(int i=0;i<k;i++) {
            List<StrategyDefinition> mutated = operators.get(i).apply(selectionClone.subList(idx, idx + sizes[i]), rng);
            result.addAll(mutated);
            idx += sizes[i];
        }

        return result;
    }
}
