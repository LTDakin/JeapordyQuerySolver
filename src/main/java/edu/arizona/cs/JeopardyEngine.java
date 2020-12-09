package edu.arizona.cs;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class JeopardyEngine {
  static boolean indexExists = false;
  Integer answeredRight = 0;
  Integer answeredTotal = 0;
  String inputDirectory = "";
  String questionFile = "";
  StandardAnalyzer analyzer;
  IndexWriterConfig config;
  IndexWriter writer;
  FSDirectory index;

  public JeopardyEngine(String directoryName, String questionFilePath) throws IOException {
    inputDirectory = directoryName;
    questionFile = questionFilePath;

    //set up the analyzer and config
    analyzer = new StandardAnalyzer();
    config = new IndexWriterConfig(analyzer);
    config.setOpenMode(OpenMode.CREATE_OR_APPEND);

    // dont remake the FSDirectory every time
    if (!indexExists) {
      System.out.println("Building a new Index ...");
      buildIndex();
    } else {
      System.out.println("Index exists already, beginning to Parse queries using old index");
      index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
    }
  }

  /**
   * Function that will build a Lucene index from the wiki files
   */
  private void buildIndex() throws IOException {
    // No index made before, making new index and writer
    index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
    writer = new IndexWriter(index, config);

    // Build Index by going through all wiki files
    final File folder = new File(inputDirectory);
    for (final File fileEntry : folder.listFiles()) {
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
            System.out.println("Title is: " + currentTitle);
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

  public static void main(String[] args) {
    try {
      String directory = "src\\main\\resources\\wikiDocs";
      String questionFile = "src\\main\\resources\\questions.txt";
      System.out.println("********Welcome to Jeopardy Engine********");
      // create jEngine object
      JeopardyEngine jEngine = new JeopardyEngine(directory, questionFile);
      // Read in queries and answer each
      System.out.println("Beginning to Answer Questions...");
      jEngine.parseQuestions(questionFile);
      // print results
      jEngine.printScore();
      System.out.println("Program completed, Thank you.");
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

  // Parses Question file, extracts question and answer checks if right, iterates
  // over all questions
  public void parseQuestions(String questionFile)
      throws java.io.FileNotFoundException, java.io.IOException, ParseException {
    Scanner scanner = new Scanner(new File(questionFile));
    // loop through queries in question file
    while (scanner.hasNextLine()) {
      String category = scanner.nextLine();
      String query = scanner.nextLine();
      String answer = scanner.nextLine();
      scanner.nextLine(); // consume the blank line

      // split answer if possible
      String[] answerArr = answer.split("|");

      // parse query using same analyzer
      query = query + " " + category;
      query = query.replaceAll("\\r\\n", "");

      Query q = new QueryParser("tokens", analyzer).parse(query);
      IndexReader reader = DirectoryReader.open(index);
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs docs = searcher.search(q, 1);
      ScoreDoc[] hits = docs.scoreDocs;

      // Compare returned answer with correct answer
      for (ScoreDoc s : hits) {
        // get doc that was picked as answer
        Document answerDoc = searcher.doc(s.doc);
        for (String ans : answerArr) {
          if (ans.equals(answerDoc.get("title"))) {
            answeredRight++;
            answeredTotal++;
            System.out.println("Correct answer: " + ans + " Correct Returned answer: " + answerDoc.get("title"));
          } else {
            answeredTotal++;
            System.out.println("Correct answer: " + ans + " Incorrect Returned answer: " + answerDoc.get("title"));
          }
        }
      }
    }
    System.out.println("All Questions answered");
    scanner.close();
  }

  // A helper method to output the accuracy of parseQuestions() and other
  // diagnostic data
  void printScore() {
    // System.out.println("Accuracy of JeopardyEngine: " + answeredRight /
    // answeredTotal);
    System.out.println("Incorrect Queries:" + (answeredTotal - answeredRight));

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
