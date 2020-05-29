package cz.zcu.kiv.nlp.ir.trec.preprocessing;


import java.util.*;

/**
 * Created by Tigi on 29.2.2016.
 */
public class BasicPreprocessing implements Preprocessing {

    Map<String, Integer> wordFrequencies = new HashMap<String, Integer>();
    Stemmer stemmer;
    Tokenizer tokenizer;
    Set<String> stopwords;
    boolean removeAccentsBeforeStemming;
    boolean removeAccentsAfterStemming;
    boolean toLowercase;
    boolean containsCRLF;

    public BasicPreprocessing(Stemmer stemmer, Tokenizer tokenizer, Set<String> stopwords,
                              boolean removeAccentsBeforeStemming, boolean removeAccentsAfterStemming,
                              boolean toLowercase, boolean containsCRLF) {
        this.stemmer = stemmer;
        this.tokenizer = tokenizer;
        this.stopwords = stopwords;
        this.removeAccentsBeforeStemming = removeAccentsBeforeStemming;
        this.removeAccentsAfterStemming = removeAccentsAfterStemming;
        this.toLowercase = toLowercase;
        this.containsCRLF = containsCRLF;

        preprocessStopWords();
    }

    public BasicPreprocessing(Stemmer stemmer, Tokenizer tokenizer, Set<String> stopwords,
                              boolean removeAccentsBeforeStemming, boolean removeAccentsAfterStemming,
                              boolean toLowercase) {
        this(stemmer, tokenizer, stopwords, removeAccentsBeforeStemming, removeAccentsAfterStemming, toLowercase, false);
    }

    private String removeCRLF(String text) {
        return text.replace("\\n", " ");
    }

    private void preprocessStopWords() {
        Set<String> preprocessedStopWords = new HashSet<String>();

        if (stopwords != null) {
            for (String word : stopwords) {
                preprocessedStopWords.add(getProcessedForm(word));
            }
        }

        stopwords = preprocessedStopWords;
    }

    @Override
    public void index(String document) {
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

            if (stemmer != null) {
                token = stemmer.stem(token);
            }
            if (removeAccentsAfterStemming) {
                token = removeAccents(token);
            }

            if (stopwords.contains(token)) continue;

            if (!wordFrequencies.containsKey(token)) {
                wordFrequencies.put(token, 0);
            }

            wordFrequencies.put(token, wordFrequencies.get(token) + 1);
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

    public Map<String, Integer> getWordFrequencies() {
        return wordFrequencies;
    }
}
