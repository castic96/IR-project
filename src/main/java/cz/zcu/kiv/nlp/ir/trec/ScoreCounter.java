package cz.zcu.kiv.nlp.ir.trec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreCounter {

    public HashMap<String, Double> computeScore(HashMap<String, List<Double>> documentsTfIdf, List<Double> queryTfIdf) {

        HashMap<String, Double> scores = new HashMap<>();
        for (Map.Entry<String, List<Double>> currentEntry : documentsTfIdf.entrySet()) {
            scores.put(currentEntry.getKey(), computeScoreForDocument(currentEntry.getValue(), queryTfIdf));
        }
        return scores;
    }

    private double computeScoreForDocument(List<Double> documentTfIdf, List<Double> queryTfIdf) {
        double nominator = 0.0;
        for (int i = 0; i < documentTfIdf.size(); i++) {
            nominator = nominator + (documentTfIdf.get(i) * queryTfIdf.get(i));
        }

        double denominator = computeVectorSize(documentTfIdf) * computeVectorSize(queryTfIdf);

        return nominator / denominator;
    }

    private double computeVectorSize(List<Double> vector) {
        double result = 0.0;
        for (Double number : vector) {
            result = result + (number * number);
        }

        return Math.pow(result, 0.5);
    }

}
