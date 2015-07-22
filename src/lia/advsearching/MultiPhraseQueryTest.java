package lia.advsearching;

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
import lia.analysis.synonym.SynonymAnalyzer;
import lia.analysis.synonym.SynonymEngine;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

// From chapter 5
public class MultiPhraseQueryTest extends TestCase {
  private IndexSearcher searcher;

  protected void setUp() throws Exception {
    Directory directory = new RAMDirectory();
    IndexWriterConfig iwConfig = new IndexWriterConfig(new WhitespaceAnalyzer());
    iwConfig.setOpenMode(OpenMode.CREATE);
    //iwConfig.setInfoStream(System.err);
    IndexWriter writer = new IndexWriter(directory, iwConfig);   
   
    Document doc1 = new Document();
    doc1.add(new TextField("field",
              "cacchio the quick brown fox jumped over the lazy dog",
              Field.Store.YES));
    writer.addDocument(doc1);
    Document doc2 = new Document();
    doc2.add(new TextField("field",
              "cavolo the fast fox hopped over the hound",
              Field.Store.YES));
    writer.addDocument(doc2);
    writer.close();
    IndexReader ireader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(ireader);
  }

  public void testBasic() throws Exception {
	  
	//cerca per tutti i documents che hanno nel field "field" i terms "quick" o "fast" seguiti da "fox" [slop=0 per default!!]
    MultiPhraseQuery query = new MultiPhraseQuery();
    query.add(new Term[] {                       // #A Any of these terms may be in first position to match 
        new Term("field", "quick"),              // #A
        new Term("field", "fast")                // #A
    });
    query.add(new Term("field", "fox"));         // #B Only one in second position
    System.out.println(query);                   //Query => field:"(quick fast) fox"

    TopDocs hits = searcher.search(query, 10);
    Document doc = searcher.doc(hits.scoreDocs[0].doc); //metcha "cavolo the fast fox hopped over the hound"
    assertEquals("fast fox match", 1, hits.totalHits);

    query.setSlop(1);							 //Query =>  field:"(quick fast) fox"~1
    hits = searcher.search(query, 10);                 //metcha anche  "cacchio the quick brown fox jumped over the lazy dog" dato che slop = 1
    assertEquals("both match", 2, hits.totalHits);
  }
  /*


  */

  public void testAgainstOR() throws Exception {
    PhraseQuery quickFox = new PhraseQuery();
    quickFox.setSlop(1);
    quickFox.add(new Term("field", "quick"));
    quickFox.add(new Term("field", "fox"));

    PhraseQuery fastFox = new PhraseQuery();
    fastFox.add(new Term("field", "fast"));
    fastFox.add(new Term("field", "fox"));

    BooleanQuery query = new BooleanQuery();         //cerca per "quick fox" e "fast fox" con slop 1 per la prima e 0 per la seconda
    query.add(quickFox, BooleanClause.Occur.SHOULD);
    query.add(fastFox, BooleanClause.Occur.SHOULD);
    TopDocs hits = searcher.search(query, 10);
    assertEquals(2, hits.totalHits);
  }

  public void testQueryParser() throws Exception {
    SynonymEngine engine = new SynonymEngine() {
        public String[] getSynonyms(String s) {
          if (s.equals("quick"))
            return new String[] {"fast"};
          else
            return null;
        }
      };

    Query q = new QueryParser("field",new StandardAnalyzer()).parse("\"quick fox\"");

    assertEquals("analyzed","field:\"(quick fast) fox\"", q.toString());
    assertTrue("parsed as MultiPhraseQuery", q instanceof MultiPhraseQuery);
  }

  private void debug(TopDocs hits) throws IOException {
    for (ScoreDoc sd : hits.scoreDocs) {
      Document doc = searcher.doc(sd.doc);
      System.out.println(sd.score + ": " + doc.get("field"));
    }

  }
}