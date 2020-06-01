package cz.zcu.kiv.nlp.ir.trec.data;

import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocRepo implements Serializable {

    private static Logger log = Logger.getLogger(DocRepo.class);

    private List<List<Document>> allDocuments = new ArrayList<>();
    private int generatedId = 1;
    private Set<String> idSet = new HashSet<>();

    private Set<String> tempIdSet = new HashSet<>();

    public void addDocumentList(List<Document> documents) {
        for (int i = 0; i < documents.size(); i++) {
            if (!isIdUnique(documents.get(i).getId())) {
                log.error("ID " + documents.get(i).getId() + " " + Messages.NOT_UNIQUE_ID.getText());
                documents.remove(documents.get(i));
            }
            else {
                idSet.add(documents.get(i).getId());
            }

        }

        tempIdSet.clear();

        allDocuments.add(documents);
    }

    public boolean isIdUnique(String id) {
        if (idSet.contains(id)) {
            return false;
        }

        return true;
    }

    public boolean dropDocumentById(String id) {
        List<Document> documentList;
        idSet.remove(id);

        for (int i = 0; i < allDocuments.size(); i++) {
            documentList = allDocuments.get(i);

            for (int j = 0; j < documentList.size(); j++) {
                if (documentList.get(j).getId().equals(id)) {
                    documentList.remove(j);
                    return true;
                }
            }
        }
        return false;
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

        //idSet.add(nextId);
        tempIdSet.add(nextId);

        generatedId++;

        return nextId;
    }

}
