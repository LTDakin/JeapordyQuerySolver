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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class JeopardyEngine {
  // changing to static so every object will know if index exists or not
  Integer answeredRight = 0;
  Integer answeredTotal = 0;
  static boolean indexExists = false;
  String inputDirectory = "";
  String questionFile = "";
  StandardAnalyzer analyzer;
  RAMDirectory index;
  IndexWriterConfig config;
  IndexWriter writer;

  public JeopardyEngine(String directoryName, String questionFilePath) throws IOException {
    inputDirectory = directoryName;
    questionFile = questionFilePath;
    buildIndex();
  }

  private void buildIndex() throws IOException {
    // Build our Index
    index = new RAMDirectory();

    // Build neccasary tools to parse documents
    analyzer = new StandardAnalyzer();
    config = new IndexWriterConfig(analyzer);
    writer = new IndexWriter(index, config);

    // Build Index by going through all wiki files
    final File folder = new File(inputDirectory);
    for (final File fileEntry : folder.listFiles()) {
      try (Scanner inputScanner = new Scanner(fileEntry)) {
        inputScanner.useDelimiter("\\]\\]|\\[\\[");
        while (inputScanner.hasNext()) {
          String wikiName = inputScanner.next();
          String wikiContents = inputScanner.next();
          System.out.println("################################################################################");
          System.out.println(wikiName);
          System.out.println("________________________________________________________________________________");
          System.out.println(wikiContents);
          addDoc(writer,wikiName, wikiContents);
        }
        inputScanner.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    writer.close();
    indexExists = true;
  }

  public static void main(String[] args) {
    try {
      String directory = "src\\main\\resources\\wikiDocs";
      String questionFile = "src\\main\\resources\\questions.txt";
      System.out.println("********Welcome to Jeopardy Engine");
      // create object to build index
      JeopardyEngine jEngine = new JeopardyEngine(directory, questionFile);

      // Read in queries and answer each
      // Search Index and Return answer
      // Check if correct or not
      // Loop
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

  //Parses Question file, extracts question and answer checks if right
  public void parseQuestions()
      throws java.io.FileNotFoundException, java.io.IOException, ParseException {
    if (!indexExists) {
      buildIndex();
    }


    String stringQuery = "information retrieval";
    // parse query using same analyzer
    Query q = new QueryParser("tokens", analyzer).parse(stringQuery);
    IndexReader reader = DirectoryReader.open(index);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs docs = searcher.search(q, 100);
    ScoreDoc[] hits = docs.scoreDocs;

    buildAnswers(hits);
  }

  // Helper function to add docs to an index
  private static void addDoc(IndexWriter indexWriter, String docID, String tokens) throws IOException {
    Document doc = new Document();
    doc.add(new StringField("docid", docID, Field.Store.YES));

    // use a string field for tokens so they are tokenized
    doc.add(new TextField("tokens", tokens, Field.Store.YES));
    indexWriter.addDocument(doc);
  }

  // Helper function to build ResultClass list to return as answers
  private List<ResultClass> buildAnswers(ScoreDoc[] hits) {
    // build answers
    List<ResultClass> ans = new ArrayList<ResultClass>();
    for (ScoreDoc s : hits) {
      Document doc = new Document();
      doc.add(new StringField("docid", "Doc" + Integer.toString(s.doc + 1), Field.Store.YES));
      ResultClass result = new ResultClass();
      result.DocName = doc;
      result.docScore = s.score;
      ans.add(result);
    }
    System.out.println("DEBUG:: ans length is: " + ans.size());
    // return query results
    return ans;
  }

}
