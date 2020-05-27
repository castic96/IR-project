package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreCounter {

    /**
     * Procedure computes score for documents.
     * @param invertedIndex
     * @param indexedQuery
     * @return Map of String document id and Double score.
     */
    public static Map<String, Double> computeScore(Map<String, List<DocInfo>> invertedIndex, Map<String, DocInfo> indexedQuery) {
        Map<String, Double> scores = new HashMap<>();

        computeScalarProduct(invertedIndex, indexedQuery, scores);

        normalizeScores(invertedIndex, indexedQuery, scores);

        return scores;
    }

    private static void computeScalarProduct(Map<String, List<DocInfo>> invertedIndex,
                                             Map<String, DocInfo> indexedQuery, Map<String, Double> scores) {

        DocInfo currentDocInfoQuery;
        List<DocInfo> currentDocList;

        for (Map.Entry<String, DocInfo> currentEntryQuery : indexedQuery.entrySet()) {
            currentDocInfoQuery = currentEntryQuery.getValue();
            currentDocList = invertedIndex.get(currentEntryQuery.getKey());

            for (DocInfo currentDoc : currentDocList) {
                computePartOfScalarProduct(currentDocInfoQuery, currentDoc, scores);
            }

        }
    }

    private static void computePartOfScalarProduct(DocInfo docInfoQuery, DocInfo docInfoDoc, Map<String, Double> scores) {
        double newElement = computeElement(docInfoQuery, docInfoDoc);
        String currentDocId = docInfoDoc.getDocumentId();
        Double oldValue;
        Double newValue;

        if (scores.containsKey(currentDocId)) {
            oldValue = scores.get(currentDocId);

            newValue = newElement + oldValue;
            scores.put(currentDocId, newValue);
        }
        else {
            scores.put(docInfoDoc.getDocumentId(), newElement);
        }

    }

    private static double computeElement(DocInfo docInfo1, DocInfo docInfo2) {
        return docInfo1.getTfidf() * docInfo2.getTfidf();
    }

    private static void normalizeScores(Map<String, List<DocInfo>> invertedIndex,
                                        Map<String, DocInfo> indexedQuery, Map<String, Double> scores) {

        double queryNorm = getQueryNorm(indexedQuery);
        double docNorm;
        Double currentValue;
        Double newValue;
        String currentDocId;

        for (Map.Entry<String, Double> currentEntry : scores.entrySet()) {
            currentDocId = currentEntry.getKey();

            docNorm = getDocNorm(invertedIndex, currentDocId);

            currentValue = currentEntry.getValue();

            newValue = (currentValue) / (docNorm * queryNorm);
            scores.put(currentDocId, newValue);
        }

    }

    private static double getQueryNorm(Map<String, DocInfo> indexedQuery) {
        double sum = 0.0;

        for (DocInfo currentQuery : indexedQuery.values()) {
            sum += Math.pow(currentQuery.getTfidf(), 2.0);
        }

        return Math.sqrt(sum);
    }

    private static double getDocNorm(Map<String, List<DocInfo>> invertedIndex, String docId) {
        double sum = 0.0;

        for (List<DocInfo> currentDocList : invertedIndex.values()) {
            for (DocInfo currentDoc : currentDocList) {

                if (currentDoc.getDocumentId().equals(docId)) {
                    sum += Math.pow(currentDoc.getTfidf(), 2.0);
                }

            }
        }

        return Math.sqrt(sum);
    }

}
