package cz.zcu.kiv.nlp.ir.trec.data;

public class DocInfo {

    String documentId;
    int count;
    double tfidf;

    public DocInfo(String documentId, int count) {
        this.documentId = documentId;
        this.count = count;
        this.tfidf = 0.0;
    }

    public int getCount() {
        return count;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void increaseCount() {
        count++;
    }

    public double getTfidf() {
        return tfidf;
    }

    public void setTfidf(double tfidf) {
        this.tfidf = tfidf;
    }

    public int documentIdHash() {
        if (documentId == null)  { return 0; }
        else { return documentId.hashCode(); }
    }
}
