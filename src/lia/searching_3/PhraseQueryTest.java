package lia.searching_3;

/**
 * Copyright Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific lan      
*/

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

// From chapter 3
public class PhraseQueryTest extends TestCase {
  private Directory dir;
  private IndexSearcher searcher;

  protected void setUp() throws IOException {
    dir = new RAMDirectory();
    
    Analyzer analyzer = new WhitespaceAnalyzer();
    IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
    iwConfig.setOpenMode(OpenMode.CREATE);
    iwConfig.setInfoStream(System.err);
    IndexWriter writer = new IndexWriter(dir, iwConfig);      
    
    
    //IndexWriter writer = new IndexWriter(dir,
     //                                    new WhitespaceAnalyzer(),
     //                                    IndexWriter.MaxFieldLength.UNLIMITED);
    
    
    Document doc = new Document();
    doc.add(new TextField("field",                                    
              "the quick brown fox jumped over the lazy dog",     
              Field.Store.YES));                           
    writer.addDocument(doc);
    writer.close();
    IndexReader ireader = DirectoryReader.open(dir);
    searcher = new IndexSearcher(ireader);
  }

  protected void tearDown() throws IOException {
   // searcher.close();
    dir.close();
  }

  private boolean matched(String[] phrase, int slop)
      throws IOException {
    PhraseQuery query = new PhraseQuery();              // 2
    query.setSlop(slop);                                // 2

    for (String word : phrase) {             // 3
      query.add(new Term("field", word));          // 3
    }                                                   // 3

    TopDocs matches = searcher.search(query, 10);
    System.out.println(query + " , hits = "+matches.totalHits);
    for(int i=0;i<matches.totalHits;i++) {
    	Document doc = searcher.doc(matches.scoreDocs[i].doc);
        System.out.println("match " + i + ": field = " + doc.get("field"));
      }
    
    return matches.totalHits > 0;
  }
  /*
    #1 Add a single test document
    #2 Create initial PhraseQuery
    #3 Add sequential phrase terms
   */
  public void testSlopComparison() throws Exception {
    String[] phrase = new String[] {"quick", "fox"};

    assertFalse("exact phrase not found", matched(phrase, 0));

    assertTrue("close enough", matched(phrase, 1));
  }

  public void testReverse() throws Exception {
    String[] phrase = new String[] {"fox", "quick"};

    assertFalse("hop flop", matched(phrase, 2));
    assertTrue("hop hop slop", matched(phrase, 3));
  }

  public void testMultiple() throws Exception {
    assertFalse("not close enough",
        matched(new String[] {"quick", "jumped", "lazy"}, 3));

    assertTrue("just enough",
        matched(new String[] {"quick", "jumped", "lazy"}, 4));

    assertFalse("almost but not quite",
        matched(new String[] {"lazy", "jumped", "quick"}, 7));

    assertTrue("bingo",
        matched(new String[] {"lazy", "jumped", "quick"}, 8));
  }

}
