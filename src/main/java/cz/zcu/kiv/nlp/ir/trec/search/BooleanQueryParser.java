package cz.zcu.kiv.nlp.ir.trec.search;

import cz.zcu.kiv.nlp.ir.trec.preprocessing.Preprocessing;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.List;

/**
 * Parser pro vyhledávání pomocí booleovských dotazů.
 * @author Zdeněk Častorál
 */
public class BooleanQueryParser {

    /**
     * Instance preprocessingu.
     */
    private Preprocessing preprocessing;

    /**
     * Lucene parser.
     */
    private PrecedenceQueryParser parser;

    /**
     * Konstruktor nastavující atributy.
     * @param preprocessing instance preprocessingu
     */
    public BooleanQueryParser(Preprocessing preprocessing) {
        this.preprocessing = preprocessing;
        this.parser = new PrecedenceQueryParser();
    }

    /**
     * Metoda parsuje dotaz v podobě Stringu a volá metodu
     * k vytvoření stromu pro vyhledávání.
     * @param query dotaz k parsování
     * @return kořen stromu pro vyhledávání
     */
    public BooleanQueryNode parseBooleanQuery(String query) {
        BooleanQueryNode root = new BooleanQueryNode();

        try {
            Query queryLucene = parser.parse(query, "");

            buildBooleanQueryTree(queryLucene, root);

        } catch(QueryNodeException e) {
            System.out.println(Messages.QUERY_PARSER_INVALID.getText());
            return null;
        }

        return root;
    }

    /**
     * Metoda rekurzivně vytvoří strom pro booleovské vyhledávání.
     * @param query dotaz k vyhledání
     * @param node uzel stromu
     */
    private void buildBooleanQueryTree(Query query, BooleanQueryNode node) {

        if (query instanceof TermQuery) {
            node.setTerm(true);
            node.setQueryStr(((TermQuery)query).getTerm().text());
            return;
        }

        BooleanQuery booleanQuery = (BooleanQuery) query;
        BooleanQueryNode newChild;

        List<BooleanClause> clauses = booleanQuery.clauses();

        for (BooleanClause currentClause : clauses) {

            newChild = new BooleanQueryNode();

            node.addDescendant(currentClause.getOccur(), newChild);

            if (currentClause.getQuery().getClass() == BooleanQuery.class) {
                newChild.setQueryStr(currentClause.getQuery().toString());
                buildBooleanQueryTree(currentClause.getQuery(), newChild);
            }
            else if (currentClause.getQuery().getClass() == TermQuery.class) {
                newChild.setQueryStr(preprocessing.getProcessedForm(currentClause.getQuery().toString()));
                newChild.setTerm(true);
            }
        }

    }

}
