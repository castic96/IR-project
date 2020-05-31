package cz.zcu.kiv.nlp.ir.trec.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedIndex implements Serializable {

    /**
     * Invertovaný index. Mapa(term -> Mapa (id dokumentu -> DocInfo)).
     */
    private Map<String, Map<String, DocInfo>> invertedIndexMap;

    /**
     * Tato mapa obsahuje hodnoty 'idf' pro termy, které mají nenulové 'idf'.
     * Mapa (term -> idf).
     */
    private Map<String, Double> idf;

    /**
     * Tato mapa obsahuje normy vektorů jednotlivých dokumentů.
     */
    private Map<String, Double> docVectorNorms;

    /**
     * Repozitář dokumentů v daném indexu.
     */
    private DocRepo documents;

    public InvertedIndex(List<Document> documents) {
        this.idf = new HashMap<>();
        this.documents = new DocRepo(documents);
    }

    public void setInvertedIndexMap(Map<String, Map<String, DocInfo>> invertedIndexMap) {
        this.invertedIndexMap = invertedIndexMap;
    }

    public Map<String, Double> getIdf() {
        return idf;
    }

    public Map<String, Map<String, DocInfo>> getInvertedIndexMap() {
        return invertedIndexMap;
    }

    public void setDocVectorNorms(Map<String, Double> docVectorNorms) {
        this.docVectorNorms = docVectorNorms;
    }

    public Map<String, Double> getDocVectorNorms() {
        return docVectorNorms;
    }
}
