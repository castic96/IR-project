package cz.zcu.kiv.nlp.ir.trec.search;

import cz.zcu.kiv.nlp.ir.trec.counter.ScoreCounter;
import cz.zcu.kiv.nlp.ir.trec.counter.TfidfCounter;
import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.Preprocessing;

import java.util.HashMap;
import java.util.Map;

public class NormalQueryEvaluator {

    private Preprocessing preprocessing;

    /**
     * Invertovaný index. Mapa(term -> Mapa (id dokumentu -> DocInfo)).
     */
    private Map<String, Map<String, DocInfo>> invertedIndex;

    /**
     * Tato mapa obsahuje hodnoty 'idf' pro dokumenty, které mají nenulové 'idf'.
     * Mapa (id dokumentu -> idf).
     */
    private Map<String, Double> idf;

    /**
     * Tato mapa obsahuje normy vektorů jednotlivých dokumentů.
     */
    private Map<String, Double> docVectorNorms;

    public NormalQueryEvaluator(Preprocessing preprocessing, Map<String, Map<String, DocInfo>> invertedIndex, Map<String, Double> idf,
                                Map<String, Double> docVectorNorms) {
        this.preprocessing = preprocessing;
        this.invertedIndex = invertedIndex;
        this.idf = idf;
        this.docVectorNorms = docVectorNorms;
    }

    public Map<String, Double> evaluateNormalQuery(String query) {
        Map<String, DocInfo> indexedQuery = new HashMap<>();

        this.preprocessing.indexQuery(query, indexedQuery, invertedIndex);

        countQueryTFIDF(indexedQuery);

        Map<String, Double> resultsMap = ScoreCounter.computeScore(invertedIndex, indexedQuery, docVectorNorms);

        return resultsMap;
    }

//    private void indexQuery(String query, Map<String, DocInfo> indexedQuery) {
//        String[] wordsInQuery;
//
//        wordsInQuery = query.split("\\s+");
//
//        for (String word : wordsInQuery) {
//            setToQueryIndex(word, indexedQuery);
//        }
//
//    }

//    private void setToQueryIndex(String word, Map<String, DocInfo> indexedQuery) {
//
//        if (invertedIndex.containsKey(word)) {
//
//            if (indexedQuery.containsKey(word)) {
//                indexedQuery.get(word).increaseCount();
//            }
//            else {
//                indexedQuery.put(word, new DocInfo("q", 1));
//            }
//
//        }
//    }

    private void countQueryTFIDF(Map<String, DocInfo> queryIndex) {
        DocInfo currentDoc;
        double currentIdf;

        for (Map.Entry<String, DocInfo> currentEntry : queryIndex.entrySet()) {

            if (idf.containsKey(currentEntry.getKey())) {
                currentDoc = currentEntry.getValue();
                currentIdf = idf.get(currentEntry.getKey());
                currentDoc.setTfidf(TfidfCounter.countW(currentIdf, TfidfCounter.countWF(currentDoc.getCount())));
            }

        }
    }
}
