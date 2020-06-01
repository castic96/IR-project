package cz.zcu.kiv.nlp.ir.trec.counter;

import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Knihovní třída pro výpočet skóre dokumentů odpovídajících zadanému dotazu.
 * @author Zdeněk Častorál
 */
public class ScoreCounter {

    /**
     * Metoda vyhodnocuje skóre pro dokumenty, které odpovídají zadanému dotazu.
     * @param invertedIndex invertovaný index
     * @param indexedQuery zaindexovaný dotaz
     * @param norms normy dokumentů
     * @return mapa -> String (id dokumentu), double (skóre dokumentu)
     */
    public static Map<String, Double> computeScore(Map<String, Map<String, DocInfo>> invertedIndex,
                                                   Map<String, DocInfo> indexedQuery, Map<String, Double> norms) {

        Map<String, Double> scores = new HashMap<>();

        computeScalarProduct(invertedIndex, indexedQuery, scores);

        normalizeScores(invertedIndex, indexedQuery, scores, norms);

        return scores;
    }

    /**
     * Metoda vypočítá skalární součin dokumentů a dotazu.
     * @param invertedIndex invertovaný index
     * @param indexedQuery zaindexovaný dotaz
     * @param scores mapa -> String (id dokumentu), double (skalární součin)
     */
    private static void computeScalarProduct(Map<String, Map<String, DocInfo>> invertedIndex,
                                             Map<String, DocInfo> indexedQuery, Map<String, Double> scores) {

        DocInfo currentDocInfoQuery;
        Map<String, DocInfo> currentDocList;

        for (Map.Entry<String, DocInfo> currentEntryQuery : indexedQuery.entrySet()) {
            currentDocInfoQuery = currentEntryQuery.getValue();
            currentDocList = invertedIndex.get(currentEntryQuery.getKey());

            for (DocInfo currentDoc : currentDocList.values()) {
                computePartOfScalarProduct(currentDocInfoQuery, currentDoc, scores);
            }

        }
    }

    /**
     * Pomocná metoda, která vypočítá skalární součin jednoho dokumentu a dotazu.
     * @param docInfoQuery dokument
     * @param docInfoDoc dotaz
     * @param scores mapa -> String (id dokumentu), double (skalární součin)
     */
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

    /**
     * Pomocná metoda, vynásobí TFIDF dvou předaných dokumentů.
     * @param docInfo1 dokument 1
     * @param docInfo2 dokument 2
     * @return součin TFIDF dokumentů
     */
    private static double computeElement(DocInfo docInfo1, DocInfo docInfo2) {
        return docInfo1.getTfidf() * docInfo2.getTfidf();
    }

    /**
     * Metoda vypočítá normy dokumentů.
     * @param invertedIndex invertovaný index
     * @param indexedQuery zaindexovaný dotaz
     * @param scores mapa jednotlivých skóre pro dané dokumenty
     * @param norms normy dokumentů
     */
    private static void normalizeScores(Map<String, Map<String, DocInfo>> invertedIndex, Map<String, DocInfo> indexedQuery,
                                        Map<String, Double> scores, Map<String, Double> norms) {

        double queryNorm = getQueryNorm(indexedQuery);
        double docNorm;
        Double currentValue;
        Double newValue;
        String currentDocId;

        for (Map.Entry<String, Double> currentEntry : scores.entrySet()) {
            currentDocId = currentEntry.getKey();

            docNorm = getDocNorm(currentDocId, norms);

            currentValue = currentEntry.getValue();

            newValue = (currentValue) / (docNorm * queryNorm);
            scores.put(currentDocId, newValue);
        }

    }

    /**
     * Pomocná metoda pro výpočet normy dotazu.
     * @param indexedQuery zaindexovaný dotaz
     * @return norma dotazu
     */
    private static double getQueryNorm(Map<String, DocInfo> indexedQuery) {
        double sum = 0.0;

        for (DocInfo currentQuery : indexedQuery.values()) {
            sum += Math.pow(currentQuery.getTfidf(), 2.0);
        }

        return Math.sqrt(sum);
    }

    /**
     * Pomocná metoda pro získání normy dokumentu.
     * @param docId id dokumentu
     * @param norms normy všech dokumentů
     * @return norma konkrétního dokumentu
     */
    private static double getDocNorm(String docId, Map<String, Double> norms) {
        return norms.get(docId);
    }

}
