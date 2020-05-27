package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.*;

import java.util.*;

/**
 *
 * Třída reprezentující index.
 *
 *
 */
public class Index implements Indexer, Searcher {

    // term -> documentId, countTermsInDocument
    private Map<String, List<DocInfo>> invertedIndex = new HashMap<>();

    private Map<String, Double> idf = new HashMap<>();

    private DocRepo documents;

    @Override
    public void index(List<Document> documents) {

        DocRepo docRepo = new DocRepo(documents);

        setTerms(documents);

        countIDF(countDF(), documents.size());

        countDocTFIDF(invertedIndex);

    }

    @Override
    public List<Result> search(String query) {
        Map<String, DocInfo> indexedQuery = new HashMap<>();

        indexQuery(query, indexedQuery);

        countQueryTFIDF(indexedQuery);

        Map<String, Double> resultsMap = ScoreCounter.computeScore(invertedIndex, indexedQuery);

        List<Result> results = convertToListOfResult(resultsMap);

        return results;
    }

    private void indexQuery(String query, Map<String, DocInfo> indexedQuery) {
        String[] wordsInQuery;

        wordsInQuery = query.split("\\s+");

        for (String word : wordsInQuery) {
            setToQueryIndex(word, indexedQuery);
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

    private List<Result> convertToListOfResult(Map<String, Double> resultsMap) {

        List<Result> results = new ArrayList<>();
        Result currentResult;
        float res;

        for (Map.Entry<String, Double> currentEntry : resultsMap.entrySet()) {
            res = currentEntry.getValue().floatValue();

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

    private void setTerms(List<Document> inputDocuments) {
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
                setToDocIndex(word, currentDocument.getId());
            }

        }

    }

    private void setToQueryIndex(String word, Map<String, DocInfo> indexedQuery) {

        if (invertedIndex.containsKey(word)) {

            if (indexedQuery.containsKey(word)) {
                indexedQuery.get(word).increaseCount();
            }
            else {
                indexedQuery.put(word, new DocInfo("q", 1));
            }

        }
    }

    private void setToDocIndex(String word, String id) {
        List<DocInfo> docsWithCurrentWord;

        if (invertedIndex.containsKey(word)) {
            docsWithCurrentWord = invertedIndex.get(word);

            DocInfo currentDoc = getDocInfoById(docsWithCurrentWord, id);

            if (currentDoc != null) {
                currentDoc.increaseCount();
            }
            else {
                docsWithCurrentWord.add(new DocInfo(id, 1));
            }

        }
        else {
            docsWithCurrentWord = new ArrayList<>();
            docsWithCurrentWord.add(new DocInfo(id, 1));
            invertedIndex.put(word, docsWithCurrentWord);
        }

    }

    private Map<String, Integer> countDF() {
        Map<String, Integer> df = new HashMap<>();

        for (Map.Entry<String, List<DocInfo>> currentEntry : invertedIndex.entrySet()) {
            df.put(currentEntry.getKey(), currentEntry.getValue().size());
        }

        return df;
    }

    private void countIDF(Map<String, Integer> df, int n) {
        double currentIDF;

        for (Map.Entry<String, Integer> currentEntry : df.entrySet()) {
            currentIDF = Math.log10((double)n / (double)currentEntry.getValue());

            if (currentIDF != 0.0) {
                idf.put(currentEntry.getKey(), currentIDF);
            }

        }
    }

    private void countDocTFIDF(Map<String, List<DocInfo>> invertedIndex) {
        double currentIdf;

        for (Map.Entry<String, List<DocInfo>> currentEntry : invertedIndex.entrySet()) {

            if (idf.containsKey(currentEntry.getKey())) {
                currentIdf = idf.get(currentEntry.getKey());

                for (DocInfo currentDoc : currentEntry.getValue()) {
                    currentDoc.setTfidf(countW(currentIdf, countWF(currentDoc.getCount())));
                }

            }
        }
    }

    private void countQueryTFIDF(Map<String, DocInfo> queryIndex) {
        DocInfo currentDoc;
        double currentIdf;

        for (Map.Entry<String, DocInfo> currentEntry : queryIndex.entrySet()) {

            if (idf.containsKey(currentEntry.getKey())) {
                currentDoc = currentEntry.getValue();
                currentIdf = idf.get(currentEntry.getKey());
                currentDoc.setTfidf(countW(currentIdf, countWF(currentDoc.getCount())));
            }

        }
    }

    private double countW(double idf, double wf) {
        return wf * idf;
    }

    private double countWF(int tf) {
        return (1.0 + Math.log10(tf));
    }

    private DocInfo getDocInfoById(List<DocInfo> documents, String id) {

        for (DocInfo currentDocument : documents) {
            if (currentDocument.getDocumentId().equals(id)) {
                return currentDocument;
            }
        }

        return null;
    }
}
