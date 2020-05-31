package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.DocumentNew;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class App {

    static Index index;

    public static void main(String args[]) {

        initialization();

        // TEST
        List<Document> documents;
        String query1;
        String query2;
        List<Result> results;
        int topResults;
        query1= "dítě si poradí";
        query2 = "vzdělání v podmínkách  příjemných pro život";
        topResults = 8;

//        List<Record> inputData = loadData(DEFAULT_INPUT_DATA);
//        documents = convertDataIntoDocument(inputData);
//        index = new Index();
//        index.index(documents);
        //END TEST

        run();

        //TEST
        results = index.search(query1);
        printResults(results, query1, topResults);
        results = index.search(query2);
        printResults(results, query2, topResults);
        //END TEST

        exit();



//        List<Document> documents;
//        String query1;
//        String query2;
//        Index index;
//        List<Result> results;
//        int topResults;
//
//
//        // Example 1
//        printTextExample1();
//        documents = addDocumentsExample1();
//        query1 = "krásné město";
//        topResults = 3;
//
//        index = new Index();
//
//        index.index(documents);
//
//        results = index.search(query1);
//        printResults(results, query1, topResults);
//
//
//        // Example 2
//        printTextExample2();
//        documents = addDocumentsExample2();
//        query1 = "fish AND NOT czechia";
//        query2 = "tropical fish";
//        topResults = 5;
//
//        index = new Index();
//
//        index.index(documents);
//
//        results = index.search(query1, SearchType.BOOLEAN);
//        printResults(results, query1, topResults);
//
//        results = index.search(query2, SearchType.NORMAL);
//        printResults(results, query2, topResults);
//
//
//        // Example 3
//        System.out.print("\n---------------------------------------------------------------------------------------------------------");
//        System.out.print("\nZadání:\n");
//        System.out.print("3) Spočtěte tf-idf váhy pro všechny dokumenty stažené v prvním cvičení: \n");
//        System.out.print("\nŘešení:\n");
//        List<Record> inputData = loadData(INPUT_DATA_FILE_NAME);
//        documents = convertDataIntoDocument(inputData);
//        query1= "dítě si poradí";
//        query2 = "vzdělání v podmínkách  příjemných pro život";
//        topResults = 8;
//
//        index = new Index();
//
//        index.index(documents);
//
//        results = index.search(query1);
//        printResults(results, query1, topResults);
//
//        results = index.search(query2);
//        printResults(results, query2, topResults);

    }

    private static void initialization() {
        System.out.print(Messages.HEADER.getText());
        System.out.print(Messages.USAGE.getText());
    }

    private static void run() {

        Shell shell = new Shell(index);

        shell.run();

        index = shell.getIndex();

    }

    private static void exit() {
        System.out.print(Messages.EXIT.getText());
        System.exit(0);
    }


    private static void printTextExample1() {
        System.out.print("\n---------------------------------------------------------------------------------------------------------");
        System.out.print("\nZadání:\n");
        System.out.print("1) Pomocí tf-idf váhy a kosinové podobnosti vypočtěte, který dokument je nejrelevantnější k dotazu q1: \n" +
                "\td1: Plzeň je krásné město a je to krásné místo\n" +
                "\td2: Ostrava je ošklivé místo\n" +
                "\td3: Praha je také krásné město Plzeň je hezčí\n\n" +
                "\tq: krásné město\n");

        System.out.print("\nŘešení:\n");
    }

    private static void printTextExample2() {
        System.out.print("\n---------------------------------------------------------------------------------------------------------");
        System.out.print("\nZadání:\n");
        System.out.print("2) Pomocí tf-idf váhy a kosinové podobnosti vypočtěte, který dokument je nejrelevantnější k dotazům q1 a q2: \n" +
                "\td1: tropical fish include fish found in tropical environments\n" +
                "\td2: fish live in a sea\n" +
                "\td3: tropical fish are popular aquarium fish\n" +
                "\td4: fish also live in Czechia\n" +
                "\td5: Czechia is a country\n\n" +
                "\tq1: tropical fish sea\n" +
                "\tq2: tropical fish\n");

        System.out.print("\nŘešení:\n");
    }

    private static List<Document> addDocumentsExample1() {
        List<Document> documents = new ArrayList<>();

        DocumentNew d1 = new DocumentNew();
        DocumentNew d2 = new DocumentNew();
        DocumentNew d3 = new DocumentNew();

        d1.setText("Plzeň je krásné město a je to krásné místo");
        d1.setTitle("d1");
        d1.setId("d1");
        d1.setDate(new Date());

        d2.setText("Ostrava je ošklivé místo");
        d2.setTitle("d2");
        d2.setId("d2");
        d2.setDate(new Date());

        d3.setText("Praha je také krásné město Plzeň je hezčí");
        d3.setTitle("d3");
        d3.setId("d3");
        d3.setDate(new Date());

        documents.add(d1);
        documents.add(d2);
        documents.add(d3);

        return documents;
    }

    private static List<Document> addDocumentsExample2() {
        List<Document> documents = new ArrayList<>();

        DocumentNew d1 = new DocumentNew();
        DocumentNew d2 = new DocumentNew();
        DocumentNew d3 = new DocumentNew();
        DocumentNew d4 = new DocumentNew();
        DocumentNew d5 = new DocumentNew();

        d1.setText("tropical fish include fish found in tropical environments");
        d1.setTitle("d1");
        d1.setId("d1");
        d1.setDate(new Date());

        d2.setText("fish live in a sea");
        d2.setTitle("d2");
        d2.setId("d2");
        d2.setDate(new Date());

        d3.setText("tropical fish are popular aquarium fish");
        d3.setTitle("d3");
        d3.setId("d3");
        d3.setDate(new Date());

        d4.setText("fish also live in Czechia");
        d4.setTitle("d4");
        d4.setId("d4");
        d4.setDate(new Date());

        d5.setText("Czechia is a country");
        d5.setTitle("d5");
        d5.setId("d5");
        d5.setDate(new Date());

        documents.add(d1);
        documents.add(d2);
        documents.add(d3);
        documents.add(d4);
        documents.add(d5);

        return documents;
    }

    private static void printResults(List<Result> results, String query, int top) {
        System.out.println("\nResults for query: \"" + query + "\"");
        for (int i = 0; i < results.size(); i++) {
            if(top == i) {
                return;
            }

            System.out.println(results.get(i).toString());
        }
    }

}
