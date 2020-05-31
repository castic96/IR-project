package cz.zcu.kiv.nlp.ir.trec.data;

import java.io.Serializable;
import java.util.List;

public class DocRepo implements Serializable {

    private List<Document> documents;

    public DocRepo(List<Document> documents) {
        this.documents = documents;
    }
}
