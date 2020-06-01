package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.*;
import cz.zcu.kiv.nlp.ir.trec.search.SearchType;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import cz.zcu.kiv.nlp.ir.trec.utils.SerializedDataHelper;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;

import java.io.File;
import java.util.*;

/**
 * Příkazový interpret. Zpracovává vstupy od uživatele a vypisuje požadované výstupy.
 * @author Zdeněk Častorál
 */
public class Shell {

    /**
     * Instance indexu.
     */
    private Index index;

    /**
     * Scanner pro načítání vstupů ze stdin.
     */
    private Scanner sc = new Scanner(System.in);

    /**
     * Defaultní cesta k datovému souboru.
     */
    private static final String DEFAULT_INPUT_DATA = "data/my_testing_data.json";

    /**
     * Defaultní nastavení počtu nejlepších výsledků.
     */
    private static final int DEFAULT_TOP_RESULTS = 10;

    /**
     * Hlavní metoda shellu. Parsuje vstup z konzole a
     * spouští specifické metody pro zpracování vstupu.
     */
    public void run() {
        String input;
        String[] splittedInput;

        String command;
        String[] parameters;

        System.out.print(Messages.PROMPT.getText());

        while (sc.hasNextLine()) {

            input = sc.nextLine();

            if (input.isEmpty()) {
                System.out.print(Messages.PROMPT.getText());
                continue;
            }

            splittedInput = input.split("\\s+");

            command = splittedInput[0].toLowerCase();
            parameters = new String[splittedInput.length - 1];

            System.arraycopy(splittedInput, 1, parameters, 0, parameters.length);

            switch (command) {
                case "exit":
                    exit(parameters);
                    return;
                case "help":
                    help(parameters);
                    break;
                case "create_index":
                    createIndex(parameters);
                    break;
                case "load_index":
                    loadIndex(parameters);
                    break;
                case "save_index":
                    saveIndex(parameters);
                    break;
                case "index_docs":
                    indexDocs(parameters);
                    break;
                case "search_normal":
                    search(parameters, false);
                    break;
                case "search_boolean":
                    search(parameters, true);
                    break;
                case "create_doc":
                    createDoc(parameters);
                    break;
                case "delete_doc":
                    deleteDoc(parameters);
                    break;
                case "update_doc":
                    updateDoc(parameters);
                    break;
                default:
                    System.out.print(Messages.UNKNOWN_COMMAND.getText());

            }
            System.out.print(Messages.PROMPT.getText());
        }
    }

    /**
     * Metoda sloužící k úpravě existujícího dokumentu.
     * @param parameters parametry příkazové řádky
     */
    private void updateDoc(String[] parameters) {
        Document newDocument;
        List<Document> documents;
        String input;

        if (parameters.length == 0) {
            System.out.print(Messages.LESS_COUNT_OF_PARAMS.getText());
            return;
        }

        if (parameters.length > 1) {
            System.out.print(Messages.MORE_COUNT_OF_PARAMS.getText());
        }

        if (index == null) {
            System.out.print(Messages.UNEXISTS_INDEX.getText());
            return;
        }

        newDocument = index.getInvertedIndex().getDocuments().getDocumentById(parameters[0]);
        documents = new ArrayList<>();

        if (!index.dropDocument(parameters[0])) {
            return;
        }

        System.out.print(Messages.ENTER_DOC_TITLE.getText());
        input = sc.nextLine();

        if (!input.isEmpty()) {
            newDocument.setTitle(input);
        }

        System.out.print(Messages.ENTER_DOC_TEXT.getText());
        input = sc.nextLine();

        if (!input.isEmpty()) {
            newDocument.setText(input);
        }

        documents.add(newDocument);

        index.index(documents);

        System.out.print(Messages.DOCS_UPDATED_SUCCEED.getText());

    }

    /**
     * Metoda sloužící k odstranění dokumentu.
     * @param parameters parametry příkazové řádky
     */
    private void deleteDoc(String[] parameters) {
        if (parameters.length == 0) {
            System.out.print(Messages.LESS_COUNT_OF_PARAMS.getText());
            return;
        }

        if (parameters.length > 1) {
            System.out.print(Messages.MORE_COUNT_OF_PARAMS.getText());
        }

        if (index == null) {
            System.out.print(Messages.UNEXISTS_INDEX.getText());
            return;
        }

        if (index.dropDocument(parameters[0])) {
            System.out.print(Messages.DROP_DOC_SUCCEED.getText());
            return;
        }
    }

    /**
     * Metoda vytvoří nový dokument v aktuálním indexu.
     * @param parameters parametry příkazové řádky
     */
    private void createDoc(String[] parameters) {
        DocumentNew newDocument;
        List<Document> documents;

        if (parameters.length > 0) {
            System.out.print(Messages.IRELEVANT_PARAMS.getText());
        }

        if (index == null) {
            System.out.print(Messages.UNEXISTS_INDEX.getText());
            return;
        }

        newDocument = new DocumentNew();
        documents = new ArrayList<>();

        System.out.print(Messages.ENTER_DOC_ID.getText());
        newDocument.setId(sc.nextLine());

        System.out.print(Messages.ENTER_DOC_TITLE.getText());
        newDocument.setTitle(sc.nextLine());

        System.out.print(Messages.ENTER_DOC_TEXT.getText());
        newDocument.setText(sc.nextLine());

        newDocument.setDate(new Date());

        documents.add(newDocument);

        index.index(documents);

        System.out.print(Messages.DOCS_INDEXED_SUCCEED.getText());

    }

    /**
     * Metoda vyhledá zadaný výraz v daném indexu.
     * @param parameters parametry příkazové řádky
     * @param isBooleanSearch přepínač, zda se jedná o vyhledávání typu boolean
     */
    private void search(String[] parameters, boolean isBooleanSearch) {
        List<Result> results;
        StringBuilder query = new StringBuilder();
        String queryStr;
        int topResults;

        if (parameters.length == 0) {
            System.out.print(Messages.LESS_COUNT_OF_PARAMS.getText());
            return;
        }

        for (int i = 0; i < parameters.length; i++) {

            query.append(parameters[i]);

            if (i < parameters.length - 1) {
                query.append(" ");
            }

        }

        System.out.print(Messages.SET_COUNT_OF_HITS.getText());

        if (sc.hasNextInt()) {
            topResults = sc.nextInt();
        }
        else {
            System.out.print(Messages.DEFAULT_TOP_HITS.getText());
            topResults = DEFAULT_TOP_RESULTS;
        }

        sc.nextLine();

        queryStr = query.toString();

        System.out.println(Messages.RESULTS_PRINT.getText() + "\"" + queryStr + "\"");

        if (isBooleanSearch) {
            results = index.search(queryStr, SearchType.BOOLEAN, topResults);
        }
        else {
            results = index.search(queryStr, SearchType.NORMAL, topResults);
        }

        if (results == null) {
            return;
        }

        printResults(results);

    }

    /**
     * Metoda zaindexuje dokumenty uložené v daném souboru.
     * @param parameters parametry příkazové řádky
     */
    private void indexDocs(String[] parameters) {
        String path;
        String[] splittedPath;
        List<Record> inputData;
        List<Document> documents;

        if (parameters.length > 1) {
            System.out.print(Messages.MORE_COUNT_OF_PARAMS.getText());
        }

        if (index == null) {
            System.out.print(Messages.UNEXISTS_INDEX.getText());
            return;
        }

        if (parameters.length == 0) {
            System.out.print(Messages.DEFAULT_JSON_PATH.getText() + DEFAULT_INPUT_DATA + "...\n");
            path = DEFAULT_INPUT_DATA;
        }
        else {
            path = parameters[0];
        }

        splittedPath = path.split("\\.");

        if (splittedPath[splittedPath.length - 1].toLowerCase().equals("json")) {
            inputData = loadData(path);

            if (inputData == null) {
                return;
            }

            documents = convertDataIntoDocument(inputData);
        }
        else if (splittedPath[splittedPath.length - 1].toLowerCase().equals("bin")) {
            File serializedData = new File(path);

            if (!serializedData.isFile()) {
                System.out.print(Messages.FILE_DOES_NOT_EXIST.getText());
                return;
            }

            documents = SerializedDataHelper.loadDocument(serializedData);
        }
        else {
            System.out.print(Messages.UNSUPPORTED_FILE_FORMAT.getText());
            return;
        }

        index.index(documents);

        System.out.print(Messages.DOCS_INDEXED_SUCCEED.getText());
    }

    /**
     * Metoda uloží aktuální index do souboru.
     * @param parameters parametry příkazové řádky
     */
    private void saveIndex(String[] parameters) {
        if (parameters.length == 0) {
            System.out.print(Messages.LESS_COUNT_OF_PARAMS.getText());
            return;
        }

        if (parameters.length > 1) {
            System.out.print(Messages.MORE_COUNT_OF_PARAMS.getText());
        }

        if (index == null) {
            System.out.print(Messages.UNEXISTS_INDEX.getText());
            return;
        }

        Utils.saveIndex(index.getInvertedIndex(), parameters[0]);

        System.out.print(Messages.SAVE_INDEX_SUCCEED.getText());

    }

    /**
     * Metoda načte index uložený v souboru.
     * @param parameters parametry příkazové řádky
     */
    private void loadIndex(String[] parameters) {
        InvertedIndex loadedIndex;

        if (parameters.length == 0) {
            System.out.print(Messages.LESS_COUNT_OF_PARAMS.getText());
            return;
        }

        if (parameters.length > 1) {
            System.out.print(Messages.MORE_COUNT_OF_PARAMS.getText());
        }

        loadedIndex = Utils.loadIndex(parameters[0]);

        if (loadedIndex == null) {
            return;
        }

        index = new Index();

        index.setInvertedIndex(loadedIndex);

        System.out.print(Messages.LOAD_INDEX_SUCCEED.getText());

    }

    /**
     * Metoda vytvoří nový index.
     * @param parameters parametry příkazové řádky
     */
    private void createIndex(String[] parameters) {
        if (parameters.length > 0) {
            System.out.print(Messages.IRELEVANT_PARAMS.getText());
        }

        if (index != null) {
            System.out.print(Messages.REPLACE_INDEX.getText());
        }

        index = new Index();

        System.out.print(Messages.CREATE_INDEX_SUCCEED.getText());
    }

    /**
     * Metoda ukončí aplikaci.
     * @param parameters parametry příkazové řádky
     */
    private void exit(String[] parameters) {
        if (parameters.length > 0) {
            System.out.print(Messages.IRELEVANT_PARAMS.getText());
        }
    }

    /**
     * Metoda vypíše nápovědu o použití aplikace.
     * @param parameters parametry příkazové řádky
     */
    private void help(String[] parameters) {
        if (parameters.length > 0) {
            System.out.print(Messages.IRELEVANT_PARAMS.getText());
        }

        System.out.print(Messages.USAGE.getText());
    }

    /**
     * Metoda načte data z JSON souboru.
     * @param fileName název souboru k načtení
     * @return list instancí třídy Record
     */
    private static List<Record> loadData(String fileName) {
        return Utils.readRecordsFromJson(fileName);
    }

    /**
     * Metoda převede data z listu objektů Record do listu objektů Document.
     * @param inputData list objektů Record
     * @return list objektů Document
     */
    private List<Document> convertDataIntoDocument(List<Record> inputData) {

        List<Document> documents = new ArrayList<>();
        DocumentNew newDocument;

        for (Record currentRecord : inputData) {
            newDocument = new DocumentNew();

            newDocument.setText(currentRecord.getBody());
            newDocument.setTitle(currentRecord.getTitle());
            newDocument.setId(index.getInvertedIndex().getDocuments().getUniqueId());
            newDocument.setDate(new Date());

            documents.add(newDocument);
        }

        return documents;
    }

    /**
     * Metoda vytiskne výsledky hledání.
     * @param results výsledky hledání uložené v listu objektů Results
     */
    private void printResults(List<Result> results) {
        Document document;
        Result result;

        System.out.println(Messages.TOP_RESULTS.getText() + results.size());

        for (int i = 0; i < results.size(); i++) {

            result = results.get(i);
            document = index.getInvertedIndex().getDocuments().getDocumentById(result.getDocumentID());

            if (document == null) {
                System.out.println(Messages.DOCUMENT_NOT_FOUND.getText());
                return;
            }

            System.out.println(result.getRank() + ".\tDocument ID: " + result.getDocumentID() + "\tScore: " + result.getScore());
            System.out.println("\tDocument title: " + document.getTitle());

            System.out.println();

        }
    }

}
