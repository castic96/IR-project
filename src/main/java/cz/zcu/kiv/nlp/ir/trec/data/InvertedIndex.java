package cz.zcu.kiv.nlp.ir.trec.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
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

    public InvertedIndex() {
        this.idf = new HashMap<>();
        this.documents = new DocRepo();
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

    public DocRepo getDocuments() {
        return documents;
    }

    public void addDocuments(List<Document> documents) {
        this.documents.addDocumentList(documents);
    }

    public boolean dropDocumentById(String id) {
        Iterator<Map.Entry<String, Map<String, DocInfo>>> iter;

        for(Map<String, DocInfo> currentTermMap : invertedIndexMap.values()) {
            if (currentTermMap.containsKey(id)) {
                currentTermMap.remove(id);
            }
        }

        iter = invertedIndexMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Map<String, DocInfo>> currentTerm = iter.next();

            if (currentTerm.getValue().size() <= 0) {
                iter.remove();
            }

        }

//        for (Map.Entry<String, Map<String, DocInfo>> currentTerm : invertedIndexMap.entrySet()) {
//            if (currentTerm.getValue().size() <= 0) {
//                invertedIndexMap.remove(currentTerm.getKey());
//            }
//        }



        return true;
    }
}
