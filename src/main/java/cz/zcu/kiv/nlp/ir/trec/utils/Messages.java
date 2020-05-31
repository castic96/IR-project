package cz.zcu.kiv.nlp.ir.trec.utils;

public enum Messages {

    HEADER("----------------------------------------------------------------------------------------------------\n" +
            "KIV/IR - SEMESTRAL WORK - APPLICATION FOR INDEXING AND SEARCHING IN DOCUMENTS.\n" +
            "----------------------------------------------------------------------------------------------------\n\n"),
    USAGE("USAGE:\nCommands:\n" +
            "\thelp\t- show a usage of the application\n" +
            "\texit\t- exit the application\n" +
            "\tcreate_index\t- create a new index\n" +
            "\tload_index <index_name>\t- load the specified index\n" +
            "\tsave_index <index_name>\t- save the current loaded index and set name of it\n" +
            "\tindex_docs <file_path>\t- index docs from JSON file specified by 'file_path' parameter\n" +
            "\tdelete_doc <doc_id>\t- delete document with specified id\n" +
            "\tsearch_normal\t- search in indexed documents using NORMAL query (without clauses AND, OR, NOT)\n" +
            "\tsearch_boolean\t- search in indexed documents using BOOLEAN query (AND, OR, NOT)\n\n" +
            "Boolean expressions:\n- boolean expressions use INFIX notation\n- examples:\n" +
            "\t- AND:\tterm1 AND term2\n" +
            "\t- OR:\tterm1 OR term2\n" +
            "\t\t\tterm1 term2\t(OR is implicit logical operator)\n" +
            "\t- NOT:\tNOT term\n" +
            "\t\t\tNOT (term1 AND term2)\n" +
            "----------------------------------------------------------------------------------------------------\n"),
    PROMPT(">> "),
    EXIT("Ending the application..."),
    UNKNOWN_COMMAND("Unknown command.\n"),
    CREATE_INDEX_SUCCEED("New index created successfully.\n"),
    REPLACE_INDEX("Dropping old index...\n"),
    IRELEVANT_PARAMS("This command does not support parameters! Ignoring superfluous parameters...\n"),
    LESS_COUNT_OF_PARAMS("Less count of parameters than expected. Ignoring command...\n"),
    MORE_COUNT_OF_PARAMS("More count of parameters than expected. Ignoring superfluous parameters...\n"),
    FILE_ALREADY_EXISTS("File with the specified name already exists. The content will be overwritten.\n"),
    SAVE_INDEX_SUCCEED("Index was saved successfully.\n"),
    LOAD_INDEX_SUCCEED("Index was loaded successfully.\n"),
    FILE_DOES_NOT_EXIST("File with the specified name does not exist. Ignoring the command...\n"),
    SAVE_UNEXISTS_INDEX("Index does not exist. Ignoring the command...\n");

    private final String text;

    Messages(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
