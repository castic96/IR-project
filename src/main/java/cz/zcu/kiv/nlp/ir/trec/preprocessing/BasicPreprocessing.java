package cz.zcu.kiv.nlp.ir.trec.preprocessing;


import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by Tigi on 29.2.2016.
 */
public class BasicPreprocessing implements Preprocessing {

    /**
     * Logger pro třídu BasicPreprocessing.
     */
    private static Logger log = Logger.getLogger(BasicPreprocessing.class);

    Stemmer stemmer;
    Tokenizer tokenizer;
    Set<String> stopwords;
    boolean removeAccentsBeforeStemming;
    boolean removeAccentsAfterStemming;
    boolean toLowercase;
    boolean containsCRLF;
    boolean ignoreSingleChar;

    public BasicPreprocessing(Stemmer stemmer, Tokenizer tokenizer, Set<String> stopwords,
                              boolean removeAccentsBeforeStemming, boolean removeAccentsAfterStemming,
                              boolean toLowercase, boolean ignoreSingleChar, boolean containsCRLF) {
        this.stemmer = stemmer;
        this.tokenizer = tokenizer;
        this.stopwords = stopwords;
        this.removeAccentsBeforeStemming = removeAccentsBeforeStemming;
        this.removeAccentsAfterStemming = removeAccentsAfterStemming;
        this.toLowercase = toLowercase;
        this.ignoreSingleChar = ignoreSingleChar;
        this.containsCRLF = containsCRLF;
    }

    public BasicPreprocessing(Stemmer stemmer, Tokenizer tokenizer, Set<String> stopwords,
                              boolean removeAccentsBeforeStemming, boolean removeAccentsAfterStemming,
                              boolean toLowercase, boolean ignoreSingleChar) {
        this(stemmer, tokenizer, stopwords, removeAccentsBeforeStemming, removeAccentsAfterStemming, toLowercase, true, false);
    }

    private String removeCRLF(String text) {
        return text.replace("\\n", " ");
    }

    @Override
    public Map<String, Map<String, DocInfo>> indexAllDocuments(List<Document> documents,
                                                               Map<String, Map<String, DocInfo>> invertedIndex) {

        double progress = 0;
        double progressStep = documents.isEmpty() ? 100 : 100.0 / documents.size();
        int progLimit = 10;

        for (Document currentDocument : documents) {

            indexDocument(currentDocument.getText(), currentDocument.getId(), invertedIndex);

            progress += progressStep;
            if (progress >= progLimit) {
                log.info("Indexing progress: " + (int)progress + " %.");
                progLimit += 10;
            }

        }

        return invertedIndex;
    }

    @Override
    public Map<String, Map<String, DocInfo>> indexAllDocuments(List<Document> documents) {
        return indexAllDocuments(documents, new HashMap<>());
    }

    public void indexDocument(String document, String id, Map<String, Map<String, DocInfo>> invertedIndex) {
        if (toLowercase) {
            document = document.toLowerCase();
        }
        if (removeAccentsBeforeStemming) {
            document = removeAccents(document);
        }
        if (containsCRLF) {
            document = removeCRLF(document);
        }

        for (String token : tokenizer.tokenize(document)) {

            if (stopwords.contains(token)) continue;

            if (stemmer != null) {
                token = stemmer.stem(token);
            }
            if (removeAccentsAfterStemming) {
                token = removeAccents(token);
            }

            if (ignoreSingleChar)
            {
                if (token.length() <= 1)
                {
                    continue;
                }
            }

            setToDocIndex(token, id, invertedIndex);
        }
    }

    public void indexQuery(String query, Map<String, DocInfo> indexedQuery, Map<String, Map<String, DocInfo>> invertedIndex) {
        if (toLowercase) {
            query = query.toLowerCase();
        }
        if (removeAccentsBeforeStemming) {
            query = removeAccents(query);
        }
        if (containsCRLF) {
            query = removeCRLF(query);
        }

        for (String token : tokenizer.tokenize(query)) {

            if (stopwords.contains(token)) continue;

            if (stemmer != null) {
                token = stemmer.stem(token);
            }
            if (removeAccentsAfterStemming) {
                token = removeAccents(token);
            }

            if (ignoreSingleChar)
            {
                if (token.length() <= 1)
                {
                    continue;
                }
            }

            setToQueryIndex(token, indexedQuery, invertedIndex);
        }
    }

    private void setToDocIndex(String word, String id, Map<String, Map<String, DocInfo>> invertedIndex) {
        Map<String, DocInfo> docsWithCurrentWord;

        if (invertedIndex.containsKey(word)) {
            docsWithCurrentWord = invertedIndex.get(word);

            DocInfo currentDoc = docsWithCurrentWord.get(id);

            if (currentDoc != null) {
                currentDoc.increaseCount();
            }
            else {
                docsWithCurrentWord.put(id, new DocInfo(id, 1));
            }

        }
        else {
            docsWithCurrentWord = new HashMap<>();
            docsWithCurrentWord.put(id, new DocInfo(id, 1));
            invertedIndex.put(word, docsWithCurrentWord);
        }
    }

    private void setToQueryIndex(String word, Map<String, DocInfo> indexedQuery, Map<String, Map<String, DocInfo>> invertedIndex) {

        if (invertedIndex.containsKey(word)) {

            if (indexedQuery.containsKey(word)) {
                indexedQuery.get(word).increaseCount();
            }
            else {
                indexedQuery.put(word, new DocInfo("q", 1));
            }

        }
    }

    @Override
    public String getProcessedForm(String text) {
        if (toLowercase) {
            text = text.toLowerCase();
        }
        if (removeAccentsBeforeStemming) {
            text = removeAccents(text);
        }

        if (stemmer != null) {
            text = stemmer.stem(text);
        }
        if (removeAccentsAfterStemming) {
            text = removeAccents(text);
        }
        return text;
    }

    final String withDiacritics = "áÁčćČĆďĎéÉěĚíÍĺĹľĽňŇńŃóÓřŘŕŔšŠśŚťŤúÚůŮýÝžŽźŹ";
    final String withoutDiacritics = "aAccCCdDeEeEiIlLlLnNnNoOrRrRsSsStTuUuUyYzZzZ";

    private String removeAccents(String text) {
        for (int i = 0; i < withDiacritics.length(); i++) {
            text = text.replaceAll("" + withDiacritics.charAt(i), "" + withoutDiacritics.charAt(i));
        }
        return text;
    }

}
