package edu.arizona.cs;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.api.Test;

public class TestJeopardyEngine {

    @Test
    public void testDocsAndScores() throws IOException, ParseException {
        String inputFileFullPath="src\\main\\resources\\wikiDocs";
        String questionFileFullPath="src\\main\\resources\\questions.txt";
        JeopardyEngine JEngine = new JeopardyEngine(inputFileFullPath, questionFileFullPath);
        JEngine.parseQuestions(questionFileFullPath);
        JEngine.printScore();
    }


}



