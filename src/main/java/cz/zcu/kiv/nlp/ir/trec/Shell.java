package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.*;
import cz.zcu.kiv.nlp.ir.trec.search.SearchType;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;
import org.apache.log4j.Logger;

import java.util.*;

public class Shell {

    /**
     * Logger pro třídu Index.
     */
    private static Logger log = Logger.getLogger(Shell.class);

    private Index index;
    private Scanner sc = new Scanner(System.in);
    private static final String DEFAULT_INPUT_DATA = "data/my_testing_data.json";
    private static final int DEFAULT_TOP_RESULTS = 10;

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

        printResults(results, queryStr);

    }

    private void indexDocs(String[] parameters) {
        String path;
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

        inputData = loadData(path);

        if (inputData == null) {
            return;
        }

        documents = convertDataIntoDocument(inputData);

        index.index(documents);

        System.out.print(Messages.DOCS_INDEXED_SUCCEED.getText());
    }

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

    private void createIndex(String[] parameters) {
        if (parameters.length > 0) {
            System.out.print(Messages.IRELEVANT_PARAMS.getText());
        }

        if (index == null) {
            index = new Index();
        }
        else {
            System.out.print(Messages.REPLACE_INDEX.getText());
        }

        System.out.print(Messages.CREATE_INDEX_SUCCEED.getText());
    }

    private void exit(String[] parameters) {
        if (parameters.length > 0) {
            System.out.print(Messages.IRELEVANT_PARAMS.getText());
        }
    }

    private void help(String[] parameters) {
        if (parameters.length > 0) {
            System.out.print(Messages.IRELEVANT_PARAMS.getText());
        }

        System.out.print(Messages.USAGE.getText());
    }

    private static List<Record> loadData(String fileName) {
        return Utils.readRecordsFromJson(fileName);
    }

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

    private void printResults(List<Result> results, String query) {
        Document document;
        Result result;

        System.out.println(Messages.TOP_RESULTS.getText() + results.size());

        for (int i = 0; i < results.size(); i++) {
//            if(top == i) {
//                return;
//            }

            result = results.get(i);
            document = index.getInvertedIndex().getDocuments().getDocumentById(result.getDocumentID());

            if (document == null) {
                log.error(Messages.DOCUMENT_NOT_FOUND.getText());
                return;
            }

            System.out.println(result.getRank() + ".\tDocument ID: " + result.getDocumentID() + "\tScore: " + result.getScore());
            System.out.println("\tDocument title: " + document.getTitle());

            System.out.println();

        }
    }

}
