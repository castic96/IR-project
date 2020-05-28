package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.*;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanClause;

import java.io.Serializable;
import java.util.*;

/**
 *
 * Třída reprezentující index.
 *
 *
 */
public class Index implements Indexer, Searcher, Serializable {

    private static Logger log = Logger.getLogger(Index.class);

    private Comparator<DocInfo> docInfoComparator;

    // term -> documentId, Doc info
    private Map<String, Map<String, DocInfo>> invertedIndex = new HashMap<>();

    private Map<String, Double> idf = new HashMap<>();

    private Map<String, Double> docVectorNorms;

    private DocRepo documents;

    public Index() {
        this.docInfoComparator = new DocInfoComparator();
    }

    @Override
    public void index(List<Document> documents) {

        this.documents = new DocRepo(documents);

        setTerms(documents);

        countIDF(countDF(), documents.size());

        countDocTFIDF(invertedIndex);

        log.info("Inverted Index size: " + invertedIndex.size());
    }

    @Override
    public List<Result> search(String query) {
        return search(query, SearchType.NORMAL);
    }

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
        Map<String, DocInfo> indexedQuery = new HashMap<>();

        indexQuery(query, indexedQuery);

        countQueryTFIDF(indexedQuery);

        Map<String, Double> resultsMap = ScoreCounter.computeScore(invertedIndex, indexedQuery, docVectorNorms);

        return convertToListOfResult(resultsMap);
    }

    private List<Result> booleanSearch(String query) {

        BooleanQueryNode root = new BooleanQueryParser().parseBooleanQuery(query);

        return evaluateBooleanQuery(root);
    }

    private List<Result> evaluateBooleanQuery(BooleanQueryNode root) {

        List<DocInfo> resultsDocInfo = computeDocInfoResults(root, false);

        if (resultsDocInfo == null) {
            return new ArrayList<>();
        }

        return convertToListOfResult(resultsDocInfo);
    }

    private List<DocInfo> computeDocInfoResults(BooleanQueryNode node, boolean isNot) {

        if (node.isTerm()) {

            // negation of term (includs NOT clause)
            if (isNot) {
                String nodeTerm = node.getQueryStr();

                Set<DocInfo> allDocInfos = new HashSet<>();

                // add all new docInfos except nodeTerm
                for (String currentTerm : invertedIndex.keySet()) {
                    if (!currentTerm.equals(nodeTerm)) {
                        allDocInfos.addAll(invertedIndex.get(currentTerm).values());
                    }
                }

                // remove all docInfos, which include nodeTerm
                if (invertedIndex.containsKey(nodeTerm)) {
                    for (DocInfo currentDocInfo : invertedIndex.get(nodeTerm).values()) {
                        allDocInfos.remove(currentDocInfo);
                    }
                }

                List<DocInfo> docInfoList = new ArrayList<>(allDocInfos);
                docInfoList.sort(docInfoComparator);

                return docInfoList;

            }
            else {
                return computeDocInfoTerm(node.getQueryStr());
            }

        }

        // node is boolean expression
        else {

            Collection<BooleanQueryNode> descendantQuery;

            List<DocInfo> results = new ArrayList<>();

            for (BooleanClause.Occur currentOccur : node.getDescendants().keySet()) {
                descendantQuery = node.getDescendants().get(currentOccur);

                switch (currentOccur) {

                    case MUST:

                        if (isNot) {

                            // NOT(a AND b) = NOT a OR NOT b
                            for (BooleanQueryNode currentDesc : descendantQuery) {
                                results = executeOr(results, computeDocInfoResults(currentDesc, true));
                            }

                        }
                        else {

                            for (BooleanQueryNode currentDesc : descendantQuery) {
                                results = executeAnd(results, computeDocInfoResults(currentDesc, false));
                            }

                        }

                        break;

                    case SHOULD:

                        if (isNot) {

                            // NOT(a OR b) = NOT a AND NOT b
                            for (BooleanQueryNode currentDesc : descendantQuery) {
                                results = executeAnd(results, computeDocInfoResults(currentDesc, true));
                            }

                        }
                        else {

                            for (BooleanQueryNode currentDesc : descendantQuery) {
                                results = executeOr(results, computeDocInfoResults(currentDesc, false));
                            }

                        }

                        break;

                    case MUST_NOT:

                        for (BooleanQueryNode currentDesc : descendantQuery) {
                            results = executeNot(results, computeDocInfoResults(currentDesc, true));
                        }

                        break;

                }

            }

            return results;
        }
    }

    private List<DocInfo> executeNot(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {
        List<DocInfo> results = isEmpty(docInfoList1, docInfoList2);

        if (results != null) {
            return results;
        }

        return executeNotForNonEmpty(docInfoList1, docInfoList2);
    }

    private List<DocInfo> executeNotForNonEmpty(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {

        List<DocInfo> results = new ArrayList<>();

        for (DocInfo currentDoc : docInfoList2) {
            if (docInfoList1.contains(currentDoc)) {
                results.add(currentDoc);
            }
        }

        return results;
    }

    private List<DocInfo> executeAnd(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {
        List<DocInfo> results = isEmpty(docInfoList1, docInfoList2);

        if (results != null) {
            return results;
        }

        return executeAndForNonEmpty(docInfoList1, docInfoList2);
    }

    private List<DocInfo> executeAndForNonEmpty(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {

        List<DocInfo> results = new ArrayList<>();

        int currentDocInfo1 = 0;
        int currentDocInfo2 = 0;

        int hashDocInfo1;
        int hashDocInfo2;

        while (currentDocInfo1 < docInfoList1.size() && currentDocInfo2 < docInfoList2.size()) {

            hashDocInfo1 = docInfoList1.get(currentDocInfo1).documentIdHash();
            hashDocInfo2 = docInfoList2.get(currentDocInfo2).documentIdHash();

            if (hashDocInfo1 == hashDocInfo2) {
                results.add(docInfoList1.get(currentDocInfo1));
                currentDocInfo1++;
                currentDocInfo2++;
            }
            else if (hashDocInfo1 < hashDocInfo2) {
                currentDocInfo1++;
            }
            else {
                currentDocInfo2++;
            }
        }

        return results;
    }

    private List<DocInfo> executeOr(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {

        List<DocInfo> results = isEmpty(docInfoList1, docInfoList2);

        if (results != null) {
            return results;
        }

        return executeOrForNonEmpty(docInfoList1, docInfoList2);

    }

    private List<DocInfo> executeOrForNonEmpty(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {

        List<DocInfo> results = new ArrayList<>();

        int currentDocInfo1 = 0;
        int currentDocInfo2 = 0;

        int hashDocInfo1;
        int hashDocInfo2;

        while (currentDocInfo1 < docInfoList1.size() && currentDocInfo2 < docInfoList2.size()) {

            hashDocInfo1 = docInfoList1.get(currentDocInfo1).documentIdHash();
            hashDocInfo2 = docInfoList2.get(currentDocInfo2).documentIdHash();

            if (hashDocInfo1 == hashDocInfo2) {
                results.add(docInfoList1.get(currentDocInfo1));

                currentDocInfo1++;
                currentDocInfo2++;
            }
            else if (hashDocInfo1 < hashDocInfo2) {
                results.add(docInfoList1.get(currentDocInfo1));
                currentDocInfo1++;
            }
            else {
                results.add(docInfoList2.get(currentDocInfo2));
                currentDocInfo2++;
            }
        }

        while (currentDocInfo1 < docInfoList1.size()) {
            results.add(docInfoList1.get(currentDocInfo1));
            currentDocInfo1++;
        }

        while (currentDocInfo2 < docInfoList2.size()) {
            results.add(docInfoList2.get(currentDocInfo2));
            currentDocInfo2++;
        }

        return results;
    }

    private List<DocInfo> isEmpty(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {

        if (docInfoList1.isEmpty() && docInfoList2.isEmpty()) return new ArrayList<>();
        else if (docInfoList1.isEmpty()) return docInfoList2;
        else if (docInfoList2.isEmpty()) return docInfoList1;

        return null;
    }

    private List<DocInfo> computeDocInfoTerm(String currentTerm) {

        if (!invertedIndex.containsKey(currentTerm)) {
            return new ArrayList<>();
        }
        else {
            List<DocInfo> docInfoList = new ArrayList<>(invertedIndex.get(currentTerm).values());
            docInfoList.sort(docInfoComparator);

            return docInfoList;
        }
    }

    private void indexQuery(String query, Map<String, DocInfo> indexedQuery) {
        String[] wordsInQuery;

        wordsInQuery = query.split("\\s+");

        for (String word : wordsInQuery) {
            setToQueryIndex(word, indexedQuery);
        }

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

    private void setTerms(List<Document> inputDocuments) {
        String[] wordsInDocument;

        double progress = 0;
        double progressStep = inputDocuments.isEmpty() ? 100 : 100.0 / inputDocuments.size();
        int progLimit = 10;

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

            progress += progressStep;
            if (progress > progLimit) {
                log.info("Indexing progress: " + progLimit + " %.");
                progLimit += 10;
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
        Map<String, DocInfo> docsWithCurrentWord;

        if (invertedIndex.containsKey(word)) {
            docsWithCurrentWord = invertedIndex.get(word);

            DocInfo currentDoc = docsWithCurrentWord.get(id);

            if (currentDoc != null) {
                currentDoc.increaseCount();
            }
            else {
                docsWithCurrentWord.put(id, new DocInfo(id, 1));
            }

        }
        else {
            docsWithCurrentWord = new HashMap<>();
            docsWithCurrentWord.put(id, new DocInfo(id, 1));
            invertedIndex.put(word, docsWithCurrentWord);
        }

    }

    private Map<String, Integer> countDF() {
        Map<String, Integer> df = new HashMap<>();

        for (Map.Entry<String, Map<String, DocInfo>> currentEntry : invertedIndex.entrySet()) {
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

    private void countDocTFIDF(Map<String, Map<String, DocInfo>> invertedIndex) {
        double currentIdf;
        double currentTfidf;
        double oldTfidf;
        DocInfo currentDocInfo;

        Map<String, Double> norms = new HashMap<>();

        for (Map.Entry<String, Map<String, DocInfo>> currentEntry : invertedIndex.entrySet()) {

            if (idf.containsKey(currentEntry.getKey())) {
                currentIdf = idf.get(currentEntry.getKey());

                for (Map.Entry<String, DocInfo> currentDoc : currentEntry.getValue().entrySet()) {
                    currentDocInfo = currentDoc.getValue();

                    currentTfidf = countW(currentIdf, countWF(currentDocInfo.getCount()));
                    currentDocInfo.setTfidf(currentTfidf);

                    if (!norms.containsKey(currentDocInfo.getDocumentId())) {
                        norms.put(currentDocInfo.getDocumentId(), 0.0);
                    }

                    oldTfidf = norms.get(currentDocInfo.getDocumentId());
                    norms.put(currentDocInfo.getDocumentId(), oldTfidf + Math.pow(currentTfidf, 2.0));
                }

            }
        }

        double normToSqrt;

        for (Map.Entry<String, Double> currentEntry : norms.entrySet()) {
            normToSqrt = currentEntry.getValue();
            norms.put(currentEntry.getKey(), Math.sqrt(normToSqrt));
        }

        docVectorNorms = norms;
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

}
