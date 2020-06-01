package cz.zcu.kiv.nlp.ir.trec.data.utils;

import cz.zcu.kiv.nlp.ir.trec.data.DocInfo;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator pro třídu DocInfo.
 * @author Zdeněk Častorál
 */
public class DocInfoComparator implements Comparator<DocInfo>, Serializable {

    @Override
    public int compare(DocInfo docInfo1, DocInfo docInfo2) {
        if (docInfo1.documentIdHash() > docInfo2.documentIdHash()) return 1;
        if (docInfo1.documentIdHash() == docInfo2.documentIdHash()) return 0;
        return -1;
    }

}
