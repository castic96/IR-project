package cz.zcu.kiv.nlp.ir;

import cz.zcu.kiv.nlp.ir.entity.Document;
import cz.zcu.kiv.nlp.ir.entity.DocumentNew;
import cz.zcu.kiv.nlp.ir.entity.Result;
import cz.zcu.kiv.nlp.ir.entity.ResultImpl;

import java.util.*;

/**
 *
 * Třída reprezentující index.
 *
 *
 */
public class Index implements Indexer, Searcher {

    // term -> documentId, countTermsInDocument
    private HashMap<String, HashMap<String, Integer>> vocabulary = new HashMap<>();

    private List<Double> idf = new ArrayList<>();

    private HashMap<String, List<Double>> tfIdf = new HashMap<>();

    @Override
    public void index(List<Document> documents) {

        setVocabulary(documents);

        List<Integer> df = countDF();

        countIDF(df, documents.size());

        tfIdf = countTFIDF(documents);

    }

    @Override
    public List<Result> search(String query) {
        String queryId = "q";
        List<Document> queryAsListOfDocuments = createQueryAsListOfDocuments(query, queryId);

        addQueryToVocabulary(queryAsListOfDocuments);

        //HashMap<String, List<Double>> queryTfidf1 = countTFIDF(queryAsListOfDocuments);
        List<Double> queryTfidf = countTFIDF(queryAsListOfDocuments).get(queryId);

        ScoreCounter sc = new ScoreCounter();
        HashMap<String, Double> resultsMap = sc.computeScore(tfIdf, queryTfidf);

        List<Result> results = convertToListOfResult(resultsMap);

        removeQueryFromVocabulary(queryAsListOfDocuments);

        return results;
    }

    private void removeQueryFromVocabulary(List<Document> queries) {

        for (Document currentDocument : queries) {

            for (Map.Entry<String, HashMap<String, Integer>> currentEntry : vocabulary.entrySet()) {
                currentEntry.getValue().remove(currentDocument.getId());
            }

        }

    }

    private void addQueryToVocabulary(List<Document> queries) {
        String[] wordsInDocument;

        for (Document currentDocument : queries) {

//            // Title
//            wordsInDocument = currentDocument.getTitle().split("\\s+");
//
//            for (String word : wordsInDocument) {
//                setWordToVocabulary(word, currentDocument.getId());
//            }

            // Text
            wordsInDocument = currentDocument.getText().split("\\s+");

            for (String word : wordsInDocument) {
                setQueryWordToVocabulary(word, currentDocument.getId());
            }

        }

    }

    private List<Document> createQueryAsListOfDocuments(String query, String id) {
        List<Document> documents = new ArrayList<>();

        DocumentNew queryAsDocument = new DocumentNew();

        queryAsDocument.setText(query);
        queryAsDocument.setTitle("q");
        queryAsDocument.setId(id);
        queryAsDocument.setDate(new Date());

        documents.add(queryAsDocument);

        return documents;
    }

    private List<Result> convertToListOfResult(HashMap<String, Double> resultsMap) {

        List<Result> results = new ArrayList<>();
        Result currentResult;
        float res;

        for (Map.Entry<String, Double> currentEntry : resultsMap.entrySet()) {
            res = currentEntry.getValue().floatValue();

            if (Float.isNaN(res)) {
                res = 0f;
            }

            currentResult = new ResultImpl(currentEntry.getKey(), res);
            results.add(currentResult);
        }

        Collections.sort(results);

        int iter = 1;

        for(Result result : results) {
            result.setRank(iter);
            iter++;
        }

        return results;

    }

    private void setVocabulary(List<Document> inputDocuments) {
        String[] wordsInDocument;

        for (Document currentDocument : inputDocuments) {

//            // Title
//            wordsInDocument = currentDocument.getTitle().split("\\s+");
//
//            for (String word : wordsInDocument) {
//                setWordToVocabulary(word, currentDocument.getId());
//            }

            // Text
            wordsInDocument = currentDocument.getText().split("\\s+");

            for (String word : wordsInDocument) {
                setWordToVocabulary(word, currentDocument.getId());
            }

        }

    }

    private void setQueryWordToVocabulary(String word, String id) {
        HashMap<String, Integer> documentsWithCurrentWord;

        if (vocabulary.containsKey(word)) {
            documentsWithCurrentWord = vocabulary.get(word);

            Integer countOfWords = documentsWithCurrentWord.get(id);

            if (countOfWords != null) {
                documentsWithCurrentWord.remove(id);
                documentsWithCurrentWord.put(id, ++countOfWords);
            }
            else {
                documentsWithCurrentWord.put(id, 1);
            }

        }

    }

    private void setWordToVocabulary(String word, String id) {
        HashMap<String, Integer> documentsWithCurrentWord;

        if (vocabulary.containsKey(word)) {
            documentsWithCurrentWord = vocabulary.get(word);

            Integer countOfWords = documentsWithCurrentWord.get(id);

            if (countOfWords != null) {
                documentsWithCurrentWord.remove(id);
                documentsWithCurrentWord.put(id, ++countOfWords);
            }
            else {
                documentsWithCurrentWord.put(id, 1);
            }

        }
        else {
            documentsWithCurrentWord = new HashMap<>();
            documentsWithCurrentWord.put(id, 1);
            vocabulary.put(word, documentsWithCurrentWord);
        }

    }

    private List<Integer> countDF() {
        List<Integer> df = new ArrayList<>();

        for (Map.Entry<String, HashMap<String, Integer>> currentEntry : vocabulary.entrySet()) {
            df.add(currentEntry.getValue().size());
        }

        return df;
    }

    private void countIDF(List<Integer> df, int n) {

        for (Integer currentFrequency : df) {
            idf.add(Math.log10((double)n / (double)currentFrequency));
        }

    }

    private HashMap<String, List<Double>> countTFIDF(List<Document> documents) {

        HashMap<String, List<Double>> allTFIDF = new HashMap<>();
        List<Double> currentTfidf;
        List<Double> currentLogTf;

        for (Document currentDocument : documents) {
            currentLogTf = countLogTF(currentDocument.getId());
            currentTfidf = multipleVectors(currentLogTf, idf);
            allTFIDF.put(currentDocument.getId(), currentTfidf);
        }

        return allTFIDF;

    }

    private List<Double> countLogTF(String id) {

        List<Double> logTf = new ArrayList<>();

        for (Map.Entry<String, HashMap<String, Integer>> currentEntry : vocabulary.entrySet()) {
            Integer countOfWords = currentEntry.getValue().get(id);

            if (countOfWords != null) {
                logTf.add(1 +  Math.log10(countOfWords));
            }
            else {
                logTf.add(0.0);
            }

        }

        return logTf;

    }

    private List<Double> multipleVectors(List<Double> v1, List<Double> v2) {

        List<Double> tfIdf = new ArrayList<>();

        if (v1.size() != v2.size()) {
            return null;
        }

        for (int i = 0; i < v1.size(); i++) {
            tfIdf.add(v1.get(i) * v2.get(i));
        }

        return tfIdf;

    }

}
