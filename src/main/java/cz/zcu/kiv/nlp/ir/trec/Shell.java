package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;
import cz.zcu.kiv.nlp.ir.trec.data.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;
import org.apache.lucene.util.fst.Util;

import java.util.Map;
import java.util.Scanner;

public class Shell {

    private Index index;
    private Scanner sc = new Scanner(System.in);

    public Shell(Index index) {
        this.index = index;
    }

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

            splittedInput = input.toLowerCase().split("\\s+");

            command = splittedInput[0];
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
                default:
                    System.out.print(Messages.UNKNOWN_COMMAND.getText());

            }
            System.out.print(Messages.PROMPT.getText());
        }
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
            System.out.print(Messages.SAVE_UNEXISTS_INDEX.getText());
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

    //TODO: jen pro test, pak smazat!
    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return this.index;
    }
}
