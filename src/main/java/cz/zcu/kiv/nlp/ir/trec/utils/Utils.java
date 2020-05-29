package cz.zcu.kiv.nlp.ir.trec.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.zcu.kiv.nlp.ir.trec.data.Record;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;


public class Utils {
    public static final java.text.DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH_mm_SS");

    /**
     * Saves text to given file.
     *
     * @param file file to save
     * @param text text to save
     */
    public static void saveFile(File file, String text) {
        try {
            PrintStream printStream = new PrintStream(new FileOutputStream(file));
            printStream.print(text);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves lines from the list into given file; each entry is saved as a new line.
     *
     * @param file file to save
     * @param list lines of text to save
     */
    public static void saveFile(File file, Collection<String> list) {
        try {

            PrintStream printStream = new PrintStream(new FileOutputStream(file));
            for (String text : list) {
                printStream.println(text);
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
    }


    /**
     * Read lines from the stream; lines are trimmed and empty lines are ignored.
     *
     * @param inputStream stream
     * @return list of lines
     */
    public static List<String> readTXTFile(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot locate stream");
        }
        try {
            List<String> result = new ArrayList<String>();

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(line.trim());
                }
            }

            inputStream.close();

            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Record> readRecordsFromJson(String fileName) {
        List<Record> records = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            Type recordListType = new TypeToken<ArrayList<Record>>(){}.getType();

            records = new Gson().fromJson(br, recordListType);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return records;
    }

    public static Set<String> loadStopWords(String file) {
        Set<String> stopWords = new HashSet<>();

        try (Stream<String> stream = Files.lines(Paths.get(file), StandardCharsets.UTF_8)) {
            stream.forEach(stopWords::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stopWords;
    }
}
