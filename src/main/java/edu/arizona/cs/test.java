package edu.arizona.cs;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;

public class test {
  static boolean indexExists = false;
  FSDirectory index;
  StandardAnalyzer analyzer;
  IndexWriterConfig config;
  IndexWriter writer;

  public test() throws IOException {
    // dont remake the FSDirectory every time
    if (!indexExists) {
      System.out.println("Building a new Index ...");
      buildIndex();
    } else {
      System.out.println("Index exists already, beginning to Parse queries using old index");
      index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
    }

    // Build neccasary tools to parse documents
    analyzer = new StandardAnalyzer();
    config = new IndexWriterConfig(analyzer);
    config.setOpenMode(OpenMode.CREATE_OR_APPEND);
    writer = new IndexWriter(index, config);
  }

  public static void main(String[] args) throws IOException {
    test newTest = new test();
  }

  private void buildIndex() throws IOException {
     // No index made before, making new index
     index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
     //Builds index etc 
     indexExists = true;
  }
}
