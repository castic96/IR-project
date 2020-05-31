package cz.zcu.kiv.nlp.ir.trec.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;
import cz.zcu.kiv.nlp.ir.trec.data.InvertedIndex;
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
            File file = new File(fileName);

            if (!file.isFile()) {
                System.out.print(Messages.FILE_DOES_NOT_EXIST.getText());
                return null;
            }

            BufferedReader br = new BufferedReader(new FileReader(file));

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

    public static void saveIndex(InvertedIndex index, String path) {
        try {
            File file = new File(path);

            if (!file.createNewFile()) {
                System.out.print(Messages.FILE_ALREADY_EXISTS.getText());
            }

            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(index);
            objectOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InvertedIndex loadIndex(String path) {
        Object object = null;

        try {
            File file = new File(path);

            if (!file.isFile()) {
                System.out.print(Messages.FILE_DOES_NOT_EXIST.getText());
                return null;
            }

            final ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

            object = objectInputStream.readObject();

            objectInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (InvertedIndex) object;
    }
}
