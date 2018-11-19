package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.math.DifferentiableFunction;
import edu.berkeley.nlp.util.IntCounter;
import java.util.Arrays;
import java.util.List;

public class MaxEntLoss implements DifferentiableFunction {
    List<MaxEntDatum> trainingData;
    double lambda;
    int featDim;

    public MaxEntLoss(List<MaxEntDatum> trainingData, double lambda) {
        this.trainingData = trainingData;
        this.featDim = AwesomeParsingReranker.featIndexer.size();
        this.lambda = lambda;
    }

    @Override
    public int dimension() {
        return featDim;
    }

    @Override
    public double valueAt(double[] weight) {
        IntCounter ic = IntCounter.wrapArray(weight, weight.length);
        double value = 0;
        value += lambda * ic.normSquared();

        for (MaxEntDatum datum : trainingData) {
            for (int pos : datum.goldFeatures)
                value -= weight[pos];
            double denom = 0.0d;

            for (int[] thisFeature : datum.features) {
                double effectiveWeights = 0.0d;
                for (int featurePos : thisFeature) {
                    effectiveWeights += weight[featurePos];
                }
                denom += Math.exp(effectiveWeights);
            }
            value += Math.log(denom);
        }
        return value;
    }

    @Override
    public double[] derivativeAt(double[] weight) {
        double[] grad = new double[featDim];
        for (MaxEntDatum datum : trainingData) {
            for (int feature : datum.goldFeatures)
                grad[feature] -= 1;
            double[] expProd = new double[datum.features.size()];
            Arrays.fill(expProd, 0);
            double denom = 0;
            for (int index = 0; index < datum.features.size(); index++) {
                int[] featureList = datum.features.get(index);
                for (int feature : featureList)
                    expProd[index] += weight[feature];
                expProd[index] = Math.exp(expProd[index]);
                denom += expProd[index];
            }
            for (int index = 0; index < datum.features.size(); index++) {
                for (int feature : datum.features.get(index))
                    grad[feature] += expProd[index] / denom;
            }
        }
        for (int index = 0; index < featDim; index++) {
            grad[index] = grad[index] / trainingData.size();
            grad[index] += 2 * lambda * weight[index];
        }
        return grad;
    }
}
