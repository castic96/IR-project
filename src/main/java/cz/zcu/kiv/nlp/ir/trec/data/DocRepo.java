package cz.zcu.kiv.nlp.ir.trec.data;

import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repozitář dokumentů uložených v daném indexu.
 * @author Zdeněk Častorál
 */
public class DocRepo implements Serializable {

    /**
     * Všechny dokumenty uložené v daném indexu.
     */
    private List<List<Document>> allDocuments = new ArrayList<>();

    /**
     * Generované unikátní id, počáteční hodnota 1;
     */
    private int generatedId = 1;

    /**
     * Množina všech id v daném indexu.
     */
    private Set<String> idSet = new HashSet<>();

    /**
     * Pomocná množina id v daném indexu, pro generování u indexace nových dokumentů.
     */
    private Set<String> tempIdSet = new HashSet<>();

    /**
     * Metoda pro přidání nového listu dokumentů do aktuálního indexu.
     * @param documents list dokumentů
     */
    public void addDocumentList(List<Document> documents) {
        for (int i = 0; i < documents.size(); i++) {
            if (!isIdUnique(documents.get(i).getId())) {
                System.out.println("ID " + documents.get(i).getId() + " " + Messages.NOT_UNIQUE_ID.getText());
                documents.remove(documents.get(i));
            }
            else {
                idSet.add(documents.get(i).getId());
            }

        }

        tempIdSet.clear();

        allDocuments.add(documents);
    }

    /**
     * Metoda, která zjišťuje, zda je zadané id unikátní či ne.
     * @param id id ke zjištění jeho unikátnosti
     * @return true - pokud je id unikátní, false - jinak
     */
    public boolean isIdUnique(String id) {
        if (idSet.contains(id)) {
            return false;
        }

        return true;
    }

    /**
     * Metoda, která odstraní dokument na základě jeho id.
     * @param id id dokumentu k odstranění
     * @return true - pokud bylo odstranění dokumentu úspěšné, false - jinak
     */
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

    /**
     * Vrací počet dokumentů v daném indexu.
     * @return počet dokumentů
     */
    public int getCountOfDocuments() {
        int count = 0;

        for (List<Document> documentList : allDocuments) {
            count += documentList.size();
        }

        return count;
    }

    /**
     * Vrací dokument na základě jeho id.
     * @param id id dokumentu
     * @return dokument
     */
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

    /**
     * Vygeneruje unikátní id.
     * @return vygenerované id
     */
    public String getUniqueId() {

        String nextId = Integer.toString(generatedId);

        while (idSet.contains(nextId)) {
            generatedId++;
            nextId = Integer.toString(generatedId);
        }

        tempIdSet.add(nextId);

        generatedId++;

        return nextId;
    }

}
