/**
 * Copyright (c) 2014, Michal Konkol
 * All rights reserved.
 */
package cz.zcu.kiv.nlp.ir.trec.preprocessing;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Konkol
 */
public class AdvancedTokenizer implements Tokenizer {
    private static final String DATE_REGEX = "([0-3]?\\d\\.{1})([01]?\\d\\.{1})([12]{1}\\d{3})|" +
            "([0-3]?\\d\\.{1})([01]?\\d\\.{1})|[0-9]\\+[01]";

    private static final String URL_REGEX = "(((https?:)?\\/\\/)?(([\\d\\w]|%[a-fA-f\\d]{2,2})+(:([\\d\\w]|" +
            "%[a-fA-f\\d]{2,2})+)?@)?([\\d\\w][-\\d\\w]{0,253}[\\d\\w]\\.)+[\\w]{2,63}(:[\\d]+)?(\\/([-+_.\\d\\w]|" +
            "%[a-fA-f\\d]{2,2})*)*(\\?(&?([-+_.\\d\\w]|%[a-fA-f\\d]{2,2})=?)*)?(#([-+_~.\\d\\w]|%[a-fA-f\\d]{2,2})*)?)";

    private static final String ASTERISK_REGEX = "([a-z-A-Z]*[*][a-z-A-Z]*)";

    private static final String DEFAULT_REGEX = "(\\d+[.,](\\d+)?)|([\\p{L}\\d]+)|(<.*?>)|([\\p{Punct}])";


    public static String[] tokenize(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);

        ArrayList<String> words = new ArrayList<String>();

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            words.add(text.substring(start, end));
        }

        String[] ws = new String[words.size()];
        ws = words.toArray(ws);

        return ws;
    }

    public static String removeAccents(String text) {
        return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    @Override
    public String[] tokenize(String text) {
        return tokenize(text, getRegex());
    }

    public static String getRegex() {
        return  DATE_REGEX + "|" + URL_REGEX + "|" + ASTERISK_REGEX + "|" + DEFAULT_REGEX;
    }
}
