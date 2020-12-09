/**
 * Author: Lloyd Dakin
 * Class: CSC 483
 * Project: IBM Watson Jeopardy project
 * Desc: This program will build the index to be used by Jeopardy Engine. 
 * Provided the wiki articles files it goes through each and creates a lucene index 
 * under the resources folder. 
 * */
package edu.arizona.cs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Field;

public class BuildIndex {
  static boolean indexExists = false;
  FSDirectory index;
  StandardAnalyzer analyzer;
  IndexWriterConfig config;
  IndexWriter writer;

  public static void main(String[] args) throws IOException {
    System.out.println("Welcome, this program will build the index. You can run Jeopardy Engine after this is run once.");
    System.out.println("Building a new Index ...");
    BuildIndex builder = new BuildIndex();
    builder.buildIndex();
  }

  /**
   * Function that will build a Lucene index from the wiki files
   */
  private void buildIndex() throws IOException {
    // No index made before, making new index and writer
    index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));

    // set up the analyzer and config
    analyzer = new StandardAnalyzer();
    config = new IndexWriterConfig(analyzer);
    config.setOpenMode(OpenMode.CREATE_OR_APPEND);
    writer = new IndexWriter(index, config);

    // Build Index by going through all wiki files
    final File folder = new File("src\\main\\resources\\wikiDocs");
    for (final File fileEntry : folder.listFiles()) {
      System.out.println("Working on file ..."+fileEntry.getName());
      try (Scanner inputScanner = new Scanner(fileEntry)) {
        String contents = "";
        // read first title
        String currentTitle = inputScanner.nextLine();
        currentTitle = removeBrackets(currentTitle);
        // read contents, create new doc if new title found
        while (inputScanner.hasNextLine()) {
          String inputLine = inputScanner.nextLine();
          // found a new document
          if (inputLine.length() > 4 && inputLine.length() < 40 && checkTitle(inputLine)) {
            addDoc(writer, currentTitle, contents);
            currentTitle = removeBrackets(inputLine);
            contents = "";
          } else {
            contents += inputLine;
          }
        }
        inputScanner.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    writer.close();
    System.out.println("...Finished Building the Index");
    indexExists = true;
  }

  /**
   * Helper function that takes a string as input and decides wheter it is or is
   * not a title of a wiki
   * 
   * @param input
   * @return a boolean whether the current string is a title or not
   */
  private boolean checkTitle(String input) {
    if (input.charAt(0) == '[' && input.charAt(1) == '[' && input.charAt(input.length() - 1) == ']'
        && input.charAt(input.length() - 2) == ']') {
      return true;
    } else
      return false;
  }

  // Helper function to add docs to the RAM index
  private static void addDoc(IndexWriter indexWriter, String title, String tokens) throws IOException {
    Document doc = new Document();
    doc.add(new StringField("title", title, Field.Store.YES));
    // use a string field for tokens so they are tokenized
    doc.add(new TextField("tokens", tokens, Field.Store.YES));
    indexWriter.addDocument(doc);
  }

  /**
   * Small helper function to remove brackets from a title string
   * 
   * @param input the string to remove brackets from
   * @return the new string
   */
  private String removeBrackets(String input) {
    input = input.replace("[", "");
    input = input.replace("]", "");
    return input;
  }

}
