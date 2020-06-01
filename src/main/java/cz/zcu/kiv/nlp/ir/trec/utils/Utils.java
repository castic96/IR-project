package cz.zcu.kiv.nlp.ir.trec.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.zcu.kiv.nlp.ir.trec.data.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.data.Record;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Knihovní třída pro práci se soubory.
 * @author Zdeněk Častorál
 */
public class Utils {

    /**
     * Načte data z daného souboru ve formátu JSON do listu Record.
     * @param fileName název souboru pro načtení dat
     * @return list záznamů (instance Record)
     */
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

    /**
     * Načte soubor se stop slovy.
     * @param file název souboru k načtení
     * @return množina stop slov
     */
    public static Set<String> loadStopWords(String file) {
        Set<String> stopWords = new HashSet<>();

        try (Stream<String> stream = Files.lines(Paths.get(file), StandardCharsets.UTF_8)) {
            stream.forEach(stopWords::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stopWords;
    }

    /**
     * Uloží index do souboru.
     * @param index index k uložení
     * @param path cesta k souboru pro uložení indexu
     */
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

    /**
     * Načte index ze souboru.
     * @param path cesta k indexu
     * @return načtený index
     */
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
