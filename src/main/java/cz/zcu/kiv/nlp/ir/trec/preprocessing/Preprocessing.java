package cz.zcu.kiv.nlp.ir.trec.preprocessing;

import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;
import cz.zcu.kiv.nlp.ir.trec.data.Document;

import java.util.List;
import java.util.Map;

/**
 * Created by tigi on 29.2.2016.
 */
public interface Preprocessing {

    String getProcessedForm(String text);

    Map<String, Map<String, DocInfo>> indexAllDocuments(List<Document> documents);

    void indexDocument(String document, String id, Map<String, Map<String, DocInfo>> invertedIndex);

    void indexQuery(String query, Map<String, DocInfo> indexedQuery, Map<String, Map<String, DocInfo>> invertedIndex);

}
