package cz.zcu.kiv.nlp.ir.trec.utils;

/**
 * Enum obsahující výpisy na konzoli
 * @author Zdeněk Častorál
 */
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
            "\tindex_docs [file_path]\t- index docs from JSON or BIN file, optionally from 'file_path' file\n" +
            "\tcreate_doc\t- create new document\n" +
            "\tupdate_doc <doc_id>\t- update document with specified id\n" +
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
    SAVE_INDEX_SUCCEED("Index has been saved successfully.\n"),
    LOAD_INDEX_SUCCEED("Index has been loaded successfully.\n"),
    FILE_DOES_NOT_EXIST("File with the specified name does not exist. Ignoring the command...\n"),
    UNEXISTS_INDEX("Index does not exist. Ignoring the command...\n"),
    DEFAULT_JSON_PATH("File path has not been entered. Using default path: "),
    DOCS_INDEXED_SUCCEED("Indexing done.\n"),
    RESULTS_PRINT("Results for query: "),
    TOP_RESULTS("Top: "),
    SET_COUNT_OF_HITS("Enter count of top x results: "),
    DEFAULT_TOP_HITS("Invalid count of top x results. Using default value top 10 results...\n"),
    QUERY_PARSER_INVALID("Invalid syntax of boolean query. Ignoring the command..."),
    DOCUMENT_NOT_FOUND("Document with the specified id not found. Ignoring the command...\n"),
    NOT_UNIQUE_ID("is already used. Document has not been indexed..."),
    ENTER_DOC_ID("Enter document id: "),
    ENTER_DOC_TITLE("Enter title of document: "),
    ENTER_DOC_TEXT("Enter text of document: "),
    DROP_DOC_SUCCEED("Document has been deleted successfully.\n"),
    DROP_DOC_FAILURE("Deleting the document failed.\n"),
    DOCS_UPDATED_SUCCEED("Updating done.\n"),
    TOTAL_COUNT_OF_RESULTS("Total count of results: "),
    UNSUPPORTED_FILE_FORMAT("This file format is not supported.\n"),
    INV_INDEX_SIZE("Size of inverted index: ");

    /**
     * Text pro každý výčet.
     */
    private final String text;

    Messages(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
