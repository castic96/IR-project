package cz.zcu.kiv.nlp.ir.trec.counter;

import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Knihovní třída pro výpočet DF, IDF, W, WF, TFIDF
 * @author Zdeněk Častorál
 */
public class TfidfCounter {

    /**
     * Vypočte frekvenci termu ve všech dokumentech.
     * @param invertedIndex invertovaný index
     * @return mapa -> String (term), Integer (DF)
     */
    public static Map<String, Integer> countDF(Map<String, Map<String, DocInfo>> invertedIndex) {
        Map<String, Integer> df = new HashMap<>();

        for (Map.Entry<String, Map<String, DocInfo>> currentEntry : invertedIndex.entrySet()) {
            df.put(currentEntry.getKey(), currentEntry.getValue().size());
        }

        return df;
    }

    /**
     * Vypočte invertovanou frekvenci termu v dokumentech.
     * @param idf mapa hodnot idf
     * @param df mapa hodnot df
     * @param n počet dokumentů
     */
    public static void countIDF(Map<String, Double> idf, Map<String, Integer> df, int n) {
        double currentIDF;

        for (Map.Entry<String, Integer> currentEntry : df.entrySet()) {
            currentIDF = Math.log10((double)n / (double)currentEntry.getValue());

            if (currentIDF != 0.0) {
                idf.put(currentEntry.getKey(), currentIDF);
            }

        }
    }

    /**
     * Vypočte hodnotu TFIDF pro dokumenty pro dané termy a vrátí normy dokumentů.
     * @param invertedIndex invertovaný index
     * @param idf mapa hodnot idf
     * @return  normy dokumentů
     */
    public static Map<String, Double> countDocTFIDF(Map<String, Map<String, DocInfo>> invertedIndex, Map<String, Double> idf) {
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

        return norms;
    }

    /**
     * Vypočte W (= složka TFIDF).
     * @param idf invertovaná frekvence termu v dokumentu
     * @param wf vážená frekvence termu
     * @return složka TFIDF
     */
    public static double countW(double idf, double wf) {
        return wf * idf;
    }

    /**
     * Vypočte hodnotu WF - vážené TF.
     * @param tf frekvence termu
     * @return vážená frekvence termu
     */
    public static double countWF(int tf) {
        return (1.0 + Math.log10(tf));
    }

}
