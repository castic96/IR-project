package cz.zcu.kiv.nlp.ir.trec.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Reprezentuje jednu položku v invertovaném seznamu pro daný term.
 * @author Zdeněk Častorál
 */
public class DocInfo implements Serializable {

    /**
     * Id dokumentu.
     */
    String documentId;

    /**
     * Počet výskytů daného termu v dokumentu (tf).
     */
    int count;

    /**
     * Složka tfidf pro daný term.
     */
    double tfidf;

    /**
     * Konstruktor nastavující atributy.
     * @param documentId id dokumentu
     * @param count frekvence termu
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocInfo docInfo = (DocInfo) o;

        return Objects.equals(documentId, docInfo.documentId);
    }

    @Override
    public int hashCode() {
        return documentId != null ? documentId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return documentId;
    }
}
