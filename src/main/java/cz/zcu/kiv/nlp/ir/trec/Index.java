package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.counter.TfidfCounter;
import cz.zcu.kiv.nlp.ir.trec.data.*;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.*;
import cz.zcu.kiv.nlp.ir.trec.search.*;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 *
 * Třída reprezentující index.
 *
 *
 */
public class Index implements Indexer, Searcher, Serializable {

    /**
     * Logger pro třídu Index.
     */
    private static Logger log = Logger.getLogger(Index.class);

    /**
     * Invertovaný index.
     */
    private InvertedIndex invertedIndex;

    private Preprocessing preprocessing;


    public Index() {
        this.preprocessing = new BasicPreprocessing(new CzechStemmerLight(), new AdvancedTokenizer(),
                Utils.loadStopWords("stopwords.txt"), false, true, true, true);
        this.invertedIndex = new InvertedIndex();
    }

    /**
     * Metoda zaindexuje předané dokumenty.
     * @param documents list dokumentů k indexaci
     */
    @Override
    public void index(List<Document> documents) {

        if (this.invertedIndex.getInvertedIndexMap() == null) {
            this.invertedIndex.setInvertedIndexMap(this.preprocessing.indexAllDocuments(documents));
        }
        else {
            this.invertedIndex.setInvertedIndexMap(this.preprocessing.indexAllDocuments(
                    documents, this.invertedIndex.getInvertedIndexMap()));
        }

        this.invertedIndex.addDocuments(documents);

        TfidfCounter.countIDF(this.invertedIndex.getIdf(),
                TfidfCounter.countDF(this.invertedIndex.getInvertedIndexMap()),
                this.invertedIndex.getDocuments().getCountOfDocuments());

        this.invertedIndex.setDocVectorNorms(
                TfidfCounter.countDocTFIDF(this.invertedIndex.getInvertedIndexMap(), this.invertedIndex.getIdf()));

        log.info("Inverted Index size: " + this.invertedIndex.getInvertedIndexMap().size());

    }

    /**
     * Metoda vyhledá relevantní dokumenty k zadanému dotazu,
     * vypočítá podobnost a vrátí seřazené výsledky.
     * @param query dotaz, který má být vyhledán v dokumentech
     * @return list výsledků pro zadaný dotaz
     */
    @Override
    public List<Result> search(String query) {
        return search(query, SearchType.NORMAL);
    }

    /**
     * Metoda vyhledá relevantní dokumenty k zadanému dotazu.
     * V případě vyhledávání typu NORMAL vypočítá podobnost
     * a vrátí seřazené výsledky.
     * @param query dotaz, který má být vyhledán v dokumentech.
     * @param searchType typ vyhledávání.
     * @return list výsledků pro zadaný dotaz.
     */
    @Override
    public List<Result> search(String query, SearchType searchType) {
        List<Result> results;

        if (searchType.equals(SearchType.BOOLEAN)) {
            results = booleanSearch(query);
        }
        else {
            results = normalSearch(query);
        }

        return results;
    }

    private List<Result> normalSearch(String query) {

        NormalQueryEvaluator normalQueryEvaluator = new NormalQueryEvaluator(preprocessing,
                invertedIndex.getInvertedIndexMap(), invertedIndex.getIdf(), invertedIndex.getDocVectorNorms());

        Map<String, Double> resultsMap = normalQueryEvaluator.evaluateNormalQuery(query);

        return convertToListOfResult(resultsMap);
    }

    private List<Result> booleanSearch(String query) {

        BooleanQueryNode root = new BooleanQueryParser(preprocessing).parseBooleanQuery(query);

        if (root == null) {
            return null;
        }

        return evaluateBooleanQuery(root);
    }

    private List<Result> evaluateBooleanQuery(BooleanQueryNode root) {

        BooleanQueryEvaluator booleanQueryEvaluator = new BooleanQueryEvaluator(invertedIndex.getInvertedIndexMap());

        List<DocInfo> resultsDocInfo = booleanQueryEvaluator.computeDocInfoResults(root, false);

        if (resultsDocInfo == null) {
            return new ArrayList<>();
        }

        return convertToListOfResult(resultsDocInfo);
    }

    private List<Result> convertToListOfResult(List<DocInfo> docInfoList) {
        Map<String, Double> docInfoMap = new HashMap<>();

        for (DocInfo currentDoc : docInfoList) {
            docInfoMap.put(currentDoc.getDocumentId(), 1.0);
        }

        return convertToListOfResult(docInfoMap);
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

    public InvertedIndex getInvertedIndex() {
        return invertedIndex;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }
}
