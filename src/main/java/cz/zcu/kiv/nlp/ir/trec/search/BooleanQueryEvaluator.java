package cz.zcu.kiv.nlp.ir.trec.search;

import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;
import cz.zcu.kiv.nlp.ir.trec.data.utils.DocInfoComparator;
import org.apache.lucene.search.BooleanClause;

import java.util.*;

/**
 * Třída pro vyhodnocení booleovského výrazu.
 * @author Zdeněk Častorál
 */
public class BooleanQueryEvaluator {

    /**
     * Comparator pro třídu DocInfo
     */
    private Comparator<DocInfo> docInfoComparator;

    /**
     * Invertovaný index. Mapa(term -> Mapa (id dokumentu -> DocInfo)).
     */
    private Map<String, Map<String, DocInfo>> invertedIndex;

    /**
     * Konstruktor nastavující atributy.
     * @param invertedIndex instance invertovaného indexu
     */
    public BooleanQueryEvaluator(Map<String, Map<String, DocInfo>> invertedIndex) {
        this.docInfoComparator = new DocInfoComparator();
        this.invertedIndex = invertedIndex;
    }

    /**
     * Metoda rekurzivně prochází a zpracovává strom pro vyhledání dotazu.
     * @param node uzel stromu
     * @param isNot logická hodnota, zda je nutné znegovat výsledek
     * @return list DocInfo
     */
    public List<DocInfo> computeDocInfoResults(BooleanQueryNode node, boolean isNot) {

        if (node.isTerm()) {

            // negace termu (obsahuje klauzuli NOT)
            if (isNot) {
                String nodeTerm = node.getQueryStr();

                Set<DocInfo> allDocInfos = new HashSet<>();

                // přidá všechny nové DocInfo mimo nodeTerm
                for (String currentTerm : invertedIndex.keySet()) {
                    if (!currentTerm.equals(nodeTerm)) {
                        allDocInfos.addAll(invertedIndex.get(currentTerm).values());
                    }
                }

                // odstraní všechny DocInfo, které obsahují nodeTerm
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

        // uzel je booleovský výraz
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

    /**
     * Metoda zpracuje klauzuli NOT.
     * @param docInfoList1 list původních DocInfo
     * @param docInfoList2 list nových DocInfo
     * @return list DocInfo
     */
    private List<DocInfo> executeNot(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {
        List<DocInfo> results = isEmpty(docInfoList1, docInfoList2);

        if (results != null) {
            return results;
        }

        return executeNotForNonEmpty(docInfoList1, docInfoList2);
    }

    /**
     * Metoda zpracuje klauzuli NOT, pokud ani jeden z listů DocInfo není prázdný.
     * @param docInfoList1 list původních DocInfo
     * @param docInfoList2 list nových DocInfo
     * @return list DocInfo
     */
    private List<DocInfo> executeNotForNonEmpty(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {

        List<DocInfo> results = new ArrayList<>();

        for (DocInfo currentDoc : docInfoList2) {
            if (docInfoList1.contains(currentDoc)) {
                results.add(currentDoc);
            }
        }

        return results;
    }

    /**
     * Metoda zpracuje klauzuli AND.
     * @param docInfoList1 list původních DocInfo
     * @param docInfoList2 list nových DocInfo
     * @return list DocInfo
     */
    private List<DocInfo> executeAnd(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {
        List<DocInfo> results = isEmpty(docInfoList1, docInfoList2);

        if (results != null) {
            return results;
        }

        return executeAndForNonEmpty(docInfoList1, docInfoList2);
    }

    /**
     * Metoda zpracuje klauzuli AND, pokud ani jeden z listů DocInfo není prázdný.
     * @param docInfoList1 list původních DocInfo
     * @param docInfoList2 list nových DocInfo
     * @return list DocInfo
     */
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

    /**
     * Metoda zpracuje klauzuli OR.
     * @param docInfoList1 list původních DocInfo
     * @param docInfoList2 list nových DocInfo
     * @return list DocInfo
     */
    private List<DocInfo> executeOr(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {
        List<DocInfo> results = isEmpty(docInfoList1, docInfoList2);

        if (results != null) {
            return results;
        }

        return executeOrForNonEmpty(docInfoList1, docInfoList2);

    }

    /**
     * Metoda zpracuje klauzuli OR, pokud ani jeden z listů DocInfo není prázdný.
     * @param docInfoList1 list původních DocInfo
     * @param docInfoList2 list nových DocInfo
     * @return list DocInfo
     */
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

    /**
     * Metoda ověří, zda je některý z předaných listů DocInfo prázný.
     * @param docInfoList1 list DocInfo
     * @param docInfoList2 list DocInfo
     * @return list DocInfo
     */
    private List<DocInfo> isEmpty(List<DocInfo> docInfoList1, List<DocInfo> docInfoList2) {

        if (docInfoList1.isEmpty() && docInfoList2.isEmpty()) return new ArrayList<>();
        else if (docInfoList1.isEmpty()) return docInfoList2;
        else if (docInfoList2.isEmpty()) return docInfoList1;

        return null;
    }

    /**
     * Zpracuje předaný term.
     * @param currentTerm term ke zpracování
     * @return list DocInfo
     */
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
}
