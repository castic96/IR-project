package cz.zcu.kiv.nlp.ir.entity;

/**
 * Created by Tigi on 8.1.2015.
 *
 * Třída představuje výsledek vrácený po vyhledávání.
 * Třídu můžete libovolně upravovat, popř. si můžete vytvořit vlastní třídu,
 * která dědí od abstraktní třídy {@link AbstractResult}
 */
public class ResultImpl extends AbstractResult {

    public ResultImpl(String documentID, float score) {
        this.documentID = documentID;
        this.rank = -1;
        this.score = score;
    }

}
