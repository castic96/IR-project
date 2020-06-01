package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.counter.TfidfCounter;
import cz.zcu.kiv.nlp.ir.trec.data.*;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.*;
import cz.zcu.kiv.nlp.ir.trec.search.*;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;

import java.io.Serializable;
import java.util.*;

/**
 * Třída reprezentující index.
 * @author Zdeněk Častorál
 */
public class Index implements Indexer, Searcher, Serializable {

    /**
     * Invertovaný index.
     */
    private InvertedIndex invertedIndex;

    /**
     * Instance preprocessingu.
     */
    private Preprocessing preprocessing;

    /**
     * Cesta k souboru, který obsahuje stop slova.
     */
    private static final String STOP_WORDS_PATH = "config/stopwords.txt";

    /**
     * Defaultní hodnota počtu top výsledků.
     */
    private static final int DEFAULT_COUNT_OF_TOP = 3000;

    /**
     * Kontruktor třídy Index, inicializuje preprocessing a vytváří instanci InvertedIndex.
     */
    public Index() {
        this.preprocessing = new BasicPreprocessing(new CzechStemmerLight(), new AdvancedTokenizer(),
                Utils.loadStopWords(STOP_WORDS_PATH), false, true, true, true);
        this.invertedIndex = new InvertedIndex();
    }

    /**
     * Metoda zaindexuje předané dokumenty.
     * @param documents list dokumentů (instance třídy Document) k indexaci
     */
    @Override
    public void index(List<Document> documents) {

        this.invertedIndex.addDocuments(documents);

        if (this.invertedIndex.getInvertedIndexMap() == null) {
            this.invertedIndex.setInvertedIndexMap(this.preprocessing.indexAllDocuments(documents));
        }
        else {
            this.invertedIndex.setInvertedIndexMap(this.preprocessing.indexAllDocuments(
                    documents, this.invertedIndex.getInvertedIndexMap()));
        }

        TfidfCounter.countIDF(this.invertedIndex.getIdf(),
                TfidfCounter.countDF(this.invertedIndex.getInvertedIndexMap()),
                this.invertedIndex.getDocuments().getCountOfDocuments());

        this.invertedIndex.setDocVectorNorms(
                TfidfCounter.countDocTFIDF(this.invertedIndex.getInvertedIndexMap(), this.invertedIndex.getIdf()));

        System.out.println(Messages.INV_INDEX_SIZE.getText() + this.invertedIndex.getInvertedIndexMap().size());

    }

    /**
     * Metoda odstraní daný dokument dle jeho id.
     * @param id id dokumentu k odstranění
     * @return true - pokud vše proběhlo správně, false - jinak
     */
    public boolean dropDocument(String id) {
        if (this.invertedIndex.getInvertedIndexMap() == null) {
            System.out.print(Messages.DOCUMENT_NOT_FOUND.getText());
            return false;
        }

        if (this.invertedIndex.getDocuments().isIdUnique(id)) {
            System.out.print(Messages.DOCUMENT_NOT_FOUND.getText());
            return false;
        }

        if (!this.invertedIndex.dropDocumentById(id)) {
            return false;
        }

        if (!this.invertedIndex.getDocuments().dropDocumentById(id)) {
            return false;
        }

        TfidfCounter.countIDF(this.invertedIndex.getIdf(),
                TfidfCounter.countDF(this.invertedIndex.getInvertedIndexMap()),
                this.invertedIndex.getDocuments().getCountOfDocuments());

        this.invertedIndex.setDocVectorNorms(
                TfidfCounter.countDocTFIDF(this.invertedIndex.getInvertedIndexMap(), this.invertedIndex.getIdf()));

        return true;
    }

    /**
     * Metoda vyhledá relevantní dokumenty k zadanému dotazu,
     * vypočítá podobnost a vrátí seřazené výsledky. Typ hledání
     * nastaven na normální. Počet top výsledků nastaven na default.
     * @param query dotaz, který má být vyhledán v dokumentech
     * @return list výsledků (instance Result) pro zadaný dotaz
     */
    @Override
    public List<Result> search(String query) {
        return search(query, SearchType.NORMAL, DEFAULT_COUNT_OF_TOP);
    }

    /**
     * Metoda vyhledá relevantní dokumenty k zadanému dotazu,
     * vypočítá podobnost a vrátí seřazené výsledky. Počet top
     * výsledků nastaven na default.
     * @param query dotaz, který má být vyhledán v dokumentech
     * @param searchType typ vyhledávání
     * @return list výsledků (instance Result) pro zadaný dotaz
     */
    public List<Result> search(String query, SearchType searchType) {
        return search(query, searchType, DEFAULT_COUNT_OF_TOP);
    }

    /**
     * Metoda vyhledá relevantní dokumenty k zadanému dotazu.
     * V případě vyhledávání typu NORMAL vypočítá podobnost
     * a vrátí seřazené výsledky.
     * @param query dotaz, který má být vyhledán v dokumentech
     * @param searchType typ vyhledávání
     * @param countOfTop počet top výsledků
     * @return list výsledků (instance Result) pro zadaný dotaz
     */
    @Override
    public List<Result> search(String query, SearchType searchType, int countOfTop) {
        List<Result> results;

        if (searchType.equals(SearchType.BOOLEAN)) {
            results = booleanSearch(query);
        }
        else {
            results = normalSearch(query);
        }

        if (results != null) {
            if (results.size() == 0) {
                results = null;
            }
        }

        if (results != null) {
            System.out.println(Messages.TOTAL_COUNT_OF_RESULTS.getText() + results.size());

            if (countOfTop > results.size()) {
                countOfTop = results.size();
            }

            results = results.subList(0, countOfTop);
        }
        else {
            System.out.println(Messages.TOTAL_COUNT_OF_RESULTS.getText() + 0);
        }

        return results;
    }

    /**
     * Metoda provádějící normální vyhledávání dle zadaného dotazu.
     * @param query dotaz pro vyhledávání
     * @return list výsledků (instance Result) pro zadaný dotaz
     */
    private List<Result> normalSearch(String query) {

        NormalQueryEvaluator normalQueryEvaluator = new NormalQueryEvaluator(preprocessing,
                invertedIndex.getInvertedIndexMap(), invertedIndex.getIdf(), invertedIndex.getDocVectorNorms());

        Map<String, Double> resultsMap = normalQueryEvaluator.evaluateNormalQuery(query);

        return convertToListOfResult(resultsMap);
    }

    /**
     * Metoda provádějící booleovské vyhledávání dle zadaného dotazu.
     * @param query dotaz pro vyhledávání
     * @return list výsledků (instance Result) pro zadaný dotaz
     */
    private List<Result> booleanSearch(String query) {

        BooleanQueryNode root = new BooleanQueryParser(preprocessing).parseBooleanQuery(query);

        if (root == null) {
            return null;
        }

        return evaluateBooleanQuery(root);
    }

    /**
     * Metoda vyhodnocuje booleovský dotaz reprezentovaný stromem.
     * @param root kořen stromu reprezentující dotaz
     * @return list výsledků (instance Result)
     */
    private List<Result> evaluateBooleanQuery(BooleanQueryNode root) {

        BooleanQueryEvaluator booleanQueryEvaluator = new BooleanQueryEvaluator(invertedIndex.getInvertedIndexMap());

        List<DocInfo> resultsDocInfo = booleanQueryEvaluator.computeDocInfoResults(root, false);

        if (resultsDocInfo == null) {
            return new ArrayList<>();
        }

        return convertToListOfResult(resultsDocInfo);
    }

    /**
     * Metoda převede list DocInfo na list Result.
     * @param docInfoList list DocInfo k převedení
     * @return list Result
     */
    private List<Result> convertToListOfResult(List<DocInfo> docInfoList) {
        Map<String, Double> docInfoMap = new HashMap<>();

        for (DocInfo currentDoc : docInfoList) {
            docInfoMap.put(currentDoc.getDocumentId(), 1.0);
        }

        return convertToListOfResult(docInfoMap);
    }

    /**
     * Metoda převede mapu -> String, double na list Result.
     * @param resultsMap mapa -> String, double
     * @return list Result
     */
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
