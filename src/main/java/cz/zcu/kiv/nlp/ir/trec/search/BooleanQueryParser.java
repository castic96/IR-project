package cz.zcu.kiv.nlp.ir.trec.search;

import cz.zcu.kiv.nlp.ir.trec.preprocessing.Preprocessing;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.List;
import java.util.Map;

public class BooleanQueryParser {

    private Preprocessing preprocessing;

    private PrecedenceQueryParser parser;

    private static Logger log = Logger.getLogger(BooleanQueryParser.class);

    public BooleanQueryParser(Preprocessing preprocessing) {
        this.preprocessing = preprocessing;
        this.parser = new PrecedenceQueryParser();
    }

    public BooleanQueryNode parseBooleanQuery(String query) {
        BooleanQueryNode root = new BooleanQueryNode();

        try {
            Query queryLucene = parser.parse(query, "");

            buildBooleanQueryTree(queryLucene, root);

            //checkTreeConsistence(query, root);

        } catch(QueryNodeException e) {
            e.printStackTrace();
        }

        return root;
    }

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
            //log.debug("currentClause: " + currentClause.toString());

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

    private void checkTreeConsistence(String query, BooleanQueryNode node) {

        if (!node.isTerm() && node.getDescendants().size() == 1) {
            Map.Entry<BooleanClause.Occur, List<BooleanQueryNode>> currentEntry =
                    node.getDescendants().entrySet().iterator().next();

            BooleanClause.Occur occur = currentEntry.getKey();

            if (occur.equals(BooleanClause.Occur.MUST_NOT) && currentEntry.getValue().size() < 2
                    && currentEntry.getValue().get(0).isTerm()) {

                throw new RuntimeException("Boolean operator NOT must not be alone!\nEntered query: " + query);

            }
        }

    }


}
