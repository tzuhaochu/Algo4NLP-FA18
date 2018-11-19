package edu.berkeley.nlp.assignments.assign1.student;

import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.MemoryUsageUtils;

import java.util.List;

public class LmFactory implements LanguageModelFactory {

    /**
     * Returns a new NgramLanguageModel; this should be an instance of a class that you implement.
     * Please see edu.berkeley.nlp.langmodel.NgramLanguageModel for the interface specification.
     *
     * @param trainingData
     */
    public NgramLanguageModel newLanguageModel(Iterable<List<String>> trainingData) {
        long startTime = System.nanoTime();
      EmpiricalTrigramLanguageModel lm = new EmpiricalTrigramLanguageModel(trainingData);
        long endTime = System.nanoTime();
        MemoryUsageUtils.printMemoryUsage();
        System.out.println("Time: "+ ((endTime-startTime)/1e9));
        return lm;

    }

}
