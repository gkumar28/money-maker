package sre.engine.strategy.watchmaker;

import sre.engine.strategy.schemaobject.strategy.tree.LogicalRuleDefinition;
import sre.engine.strategy.schemaobject.strategy.tree.RuleDefinition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class EvolutionUtil {

    private EvolutionUtil() {}

    public static RuleDefinition swap(RuleDefinition node, RuleDefinition selected, RuleDefinition target) {
        if (null == node || null == selected) {
            return node;
        }

        if (node == selected) {
            return target;
        }

        if (node instanceof LogicalRuleDefinition nonLeaf) {
            List<RuleDefinition> newChildren = new ArrayList<>();
            boolean isChanged = false;

            for (RuleDefinition child: nonLeaf.getChildren()) {
                RuleDefinition newChild = swap(child, selected, target);
                if (null != newChild) {
                    newChildren.add(newChild);
                }
                isChanged = isChanged || child != newChild;
            }

            if (isChanged) {
                return nonLeaf.withChildren(newChildren);
            }
        }

        return node;
    }

    public static RuleDefinition sample(RuleDefinition ruleDefinition, Random rng, Class<? extends RuleDefinition> sampleClass)  {
        int index = 0;
        RuleDefinition result = null;
        Deque<RuleDefinition> stack = new ArrayDeque<>();
        stack.push(ruleDefinition);
        while (!stack.isEmpty()) {
            RuleDefinition current = stack.pop();

            if (sampleClass.isInstance(current)) {
                result = rng.nextDouble() < 1.0 / ++index ? current : result;
            }

            if (current instanceof LogicalRuleDefinition nonLeaf) {
                List<RuleDefinition> children = nonLeaf.getChildren();
                for (int i = children.size() - 1; i >= 0; i--) {
                    RuleDefinition child = children.get(i);
                    stack.push(child);
                }
            }
        }

        return result;
    }
}
