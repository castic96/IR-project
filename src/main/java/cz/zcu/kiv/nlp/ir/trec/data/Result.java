package cz.zcu.kiv.nlp.ir.trec.data;

public interface Result extends Comparable<Result> {

    /**
     * Vrátí id dokumentu
     * @return id dokumentu
     */
    String getDocumentID();

    /**
     * Vrátí skóre podobnosti mezi dokumentem a dotazem
     * např. kosinova podobnost
     *
     * @return skóre podobnosti mezi dokumentem a dotazem
     */
    float getScore();

    /**
     * Pořadí mezi ostatními vrácenými dokumenty
     * Výsledek s rank 1 je nejrelevantnější dokument k zadanému dotazu
     *
     * @return pořadí mezi ostatními vrácenými dokumenty
     */
    int getRank();

    void setRank(int rank);

    /**
     * Metoda používaná pro generování výstupu pro vyhodnocovací skript.
     * Metodu nepřepisujte (v potomcích) ani neupravujte
     */
    String toString(String topic);
}
