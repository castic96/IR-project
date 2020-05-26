package cz.zcu.kiv.nlp.ir.trec.data;

import java.io.*;
import java.util.List;


public class Record implements Serializable {
    private String title;
    private String date;
    private String author;
    private String source;
    private String perex;
    private String body;

    private String url;
    private String downloadedDate;

    public Record() {
    }

    public Record(String title, String date, String author, String source, String perex,
                  String body, String url, String downloadedDate) {
        this.title = title;
        this.date = date;
        this.author = author;
        this.source = source;
        this.perex = perex;
        this.body = body;
        this.url = url;
        this.downloadedDate = downloadedDate;
    }

    @Override
    public String toString() {
        return "Record{" +
                "title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", perex='" + perex + '\'' +
                ", body='" + body + '\'' +
                ", url='" + url + '\'' +
                ", downloadedDate=" + downloadedDate +
                '}';
    }

    public static void save(String path, List<Record> list) {
        try {
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(list);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in /tmp/employee.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static List<Record> load(File serializedFile) {
        final Object object;
        try {
            final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializedFile));
            object = objectInputStream.readObject();
            objectInputStream.close();
            return (List<Record>) object;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPerex() {
        return perex;
    }

    public void setPerex(String perex) {
        this.perex = perex;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDownloadedDate() {
        return downloadedDate;
    }

    public void setDownloadedDate(String downloadedDate) {
        this.downloadedDate = downloadedDate;
    }
}
