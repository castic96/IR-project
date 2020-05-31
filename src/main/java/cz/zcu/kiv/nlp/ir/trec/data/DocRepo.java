package cz.zcu.kiv.nlp.ir.trec.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocRepo implements Serializable {

    private List<List<Document>> allDocuments = new ArrayList<>();
    private int generatedId = 1;
    private Set<String> idSet = new HashSet<>();

    public void addDocumentList(List<Document> documents) {
        allDocuments.add(documents);
    }

    public int getCountOfDocuments() {
        int count = 0;

        for (List<Document> documentList : allDocuments) {
            count += documentList.size();
        }

        return count;
    }

    public Document getDocumentById(String id) {

        if (!idSet.contains(id)) {
            return null;
        }

        for(List<Document> documentList : allDocuments) {
            for(Document document : documentList) {
                if (document.getId().equals(id)) {
                    return document;
                }
            }
        }

        return null;
    }

    public String getUniqueId() {

        String nextId = Integer.toString(generatedId);

        while (idSet.contains(nextId)) {
            generatedId++;
            nextId = Integer.toString(generatedId);
        }

        idSet.add(nextId);

        generatedId++;

        return nextId;
    }

}
