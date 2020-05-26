package cz.zcu.kiv.nlp.ir.trec;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.zcu.kiv.nlp.ir.trec.data.Record;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class Utils {
    public static final java.text.DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH_mm_SS");

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
}
