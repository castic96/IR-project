package cz.zcu.kiv.nlp.ir.trec.search;

import org.apache.lucene.search.BooleanClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uzel stromu pro vyhledávání.
 * @author Zdeněk Častorál
 */
public class BooleanQueryNode {

    /**
     * Potomci daného uzlu.
     */
    private Map<BooleanClause.Occur, List<BooleanQueryNode>> descendants;

    /**
     * Dotaz v podobě řetězce.
     */
    private String queryStr;

    /**
     * Logická hodnota, zda se jedná o term či ne.
     */
    private boolean isTerm;

    public BooleanQueryNode() {
        this.descendants = new HashMap<>();
        isTerm = false;
    }

    public void addDescendant(BooleanClause.Occur currentOccur, BooleanQueryNode descendant) {

        if (!this.descendants.containsKey(currentOccur)) {
            this.descendants.put(currentOccur, new ArrayList<>());
        }

        this.descendants.get(currentOccur).add(descendant);
    }

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }

    public boolean isTerm() {
        return isTerm;
    }

    public void setTerm(boolean term) {
        isTerm = term;
    }

    public Map<BooleanClause.Occur, List<BooleanQueryNode>> getDescendants() {
        return descendants;
    }
}
