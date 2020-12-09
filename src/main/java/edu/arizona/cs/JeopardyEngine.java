
/**
 * Author: Lloyd Dakin
 * Class: CSC 483
 * Project: IBM Watson Jeopardy project
 * Desc: This is the query answering class for the project, after the index is created
 * this class can be run using main, and it will do its best to answer the question. 
 * You can find diagnostic print commands in the ParseQuestions() function if you would like
 * to see when the program gets the correct answer and when it messes up.
 * */
package edu.arizona.cs;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class JeopardyEngine {
  boolean bm25 = false;
  boolean cosine = false;
  Double answeredRight = 0.0;
  Double answeredTotal = 0.0;
  String inputDirectory = "";
  String questionFile = "";
  StandardAnalyzer analyzer;
  IndexWriterConfig config;
  IndexWriter writer;
  FSDirectory index;

  /**
   * Constructor for Jeopardy Engine
   * 
   * @args String directoryName file path for wiki file, String questionFile path
   *       file path for questions
   */
  public JeopardyEngine(String directoryName, String questionFilePath) throws IOException {
    inputDirectory = directoryName;
    questionFile = questionFilePath;

    // set up the analyzer and config
    analyzer = new StandardAnalyzer();
    config = new IndexWriterConfig(analyzer);
    config.setOpenMode(OpenMode.CREATE_OR_APPEND);

    System.out.println("Index exists already, beginning to Parse queries using old index");
    index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
  }

  /**
   * Main method for the JeopardyEngine
   */
  public static void main(String[] args) {
    try {
      String directory = "src\\main\\resources\\wikiDocs";
      String questionFile = "src\\main\\resources\\questions.txt";
      System.out.println("********Welcome to Jeopardy Engine********");
      // create jEngine object
      JeopardyEngine jEngine = new JeopardyEngine(directory, questionFile);

      System.out.println("What scoring would you like to use?\n\t[1]: BM25\n\t[2]: Cosine-Similarity");
      Scanner myInput = new Scanner(System.in);
      int selection = myInput.nextInt();
      switch (selection) {
        case 1:
          System.out.println("BM25 scoring selected");
          jEngine.bm25 = true;
          break;
        case 2:
          System.out.println("Cosine-Similarity scoring selected");
          jEngine.cosine = true;
          break;
        default:
          System.out.println("Please rerun the program and select 1 or 2");
          System.exit(0);
          break;
      }
      myInput.close();

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

  /**
   * Parses Question file, extracts question and answer checks if right, iterates
   * over all questions
   */
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
      String[] answerArr = answer.split("\\|");

      // parse query using same analyzer
      query = query + " " + category;
      query = query.replaceAll("\\r\\n", "");
      // System.out.println("The question is: " + query);

      Query q = new QueryParser("tokens", analyzer).parse(QueryParser.escape(query));
      IndexReader reader = DirectoryReader.open(index);
      IndexSearcher searcher = new IndexSearcher(reader);
      // if cosine similarity selected, change to cosine similarity
      if (cosine) {
        searcher.setSimilarity(new ClassicSimilarity());
      }

      TopDocs docs = searcher.search(q, 1);
      ScoreDoc[] hits = docs.scoreDocs;

      // Compare returned answer with correct answer
      for (ScoreDoc s : hits) {
        // get doc that was picked as answer
        Document answerDoc = searcher.doc(s.doc);
        for (String ans : answerArr) {
          // cleaning out whitespace from the answer
          ans = ans.trim();
          if (ans.equals(answerDoc.get("title"))) {
            answeredRight++;
            // System.out.println("Correct answer: " + ans + " \tCorrect Returned answer: "
            // + answerDoc.get("title"));
            break;
          } else {
            //System.out.println("The question is: " + query);
            //System.out.println("Correct answer: " + ans + " \tIncorrect Returned answer:" + answerDoc.get("title"));
          }
        }
      }
      answeredTotal++;
    }
    System.out.println("All Questions answered");
    scanner.close();
  }

  /**
   * A helper method to output the accuracy of parseQuestions() and other
   * diagnostic data
   */
  void printScore() {
    Double accuracy = answeredRight / answeredTotal * 100;
    System.out.println("\tAccuracy of JeopardyEngine: " + String.format("%.0f", accuracy) + "%");
    System.out.println("\tCorrect Queries: " + String.format("%.0f", answeredRight));
    System.out.println("\tIncorrect Queries: " + String.format("%.0f", (answeredTotal - answeredRight)));
  }
}
