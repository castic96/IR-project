package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.*;
import cz.zcu.kiv.nlp.ir.trec.search.SearchType;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;

import java.util.*;

public class Shell {

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
                default:
                    System.out.print(Messages.UNKNOWN_COMMAND.getText());

            }
            System.out.print(Messages.PROMPT.getText());
        }
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

        queryStr = query.toString();

        if (isBooleanSearch) {
            results = index.search(queryStr, SearchType.BOOLEAN);
        }
        else {
            results = index.search(queryStr, SearchType.NORMAL);
        }

        if (results == null) {
            return;
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

        printResults(results, queryStr, topResults);

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

    private void printResults(List<Result> results, String query, int top) {

        Result result;

        System.out.println(Messages.RESULTS_PRINT.getText() + "\"" + query + "\"");
        System.out.println(Messages.TOP_RESULTS.getText() + top);

        for (int i = 0; i < results.size(); i++) {
            if(top == i) {
                return;
            }

            result = results.get(i);

            System.out.println(result.getRank() + ".\tDocument ID: " + result.getDocumentID() + "\tScore: " + result.getScore());
            System.out.println("\tDocument title: " + index.getInvertedIndex().getDocuments().getDocumentById(result.getDocumentID()).getTitle());

            System.out.println();

        }
    }

}
