package lia.advsearching_5;

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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;

// From chapter 5
public class SpanQueryTest extends TestCase {
  private RAMDirectory directory;
  private IndexSearcher searcher;
  private IndexReader reader;

  private SpanTermQuery quick;
  private SpanTermQuery brown;
  private SpanTermQuery red;
  private SpanTermQuery fox;
  private SpanTermQuery lazy;
  private SpanTermQuery sleepy;
  private SpanTermQuery dog;
  private SpanTermQuery cat;
  private Analyzer analyzer;

  protected void setUp() throws Exception {
    directory = new RAMDirectory();
    analyzer = new WhitespaceAnalyzer(); 
    IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
    iwConfig.setOpenMode(OpenMode.CREATE);
    IndexWriter writer = new IndexWriter(directory, iwConfig);           //3
    
    
    Document doc = new Document();
    doc.add(new TextField("field",
        "the quick brown fox jumps over the lazy dog",
        Field.Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("field",
        "the quick red fox jumps over the sleepy cat",
        Field.Store.YES));
    writer.addDocument(doc);

    writer.close();
    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);

    quick = new SpanTermQuery(new Term("field", "quick"));
    brown = new SpanTermQuery(new Term("field", "brown"));
    red = new SpanTermQuery(new Term("field", "red"));
    fox = new SpanTermQuery(new Term("field", "fox"));
    lazy = new SpanTermQuery(new Term("field", "lazy"));
    sleepy = new SpanTermQuery(new Term("field", "sleepy"));
    dog = new SpanTermQuery(new Term("field", "dog"));
    cat = new SpanTermQuery(new Term("field", "cat"));
  }

  private void assertOnlyBrownFox(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(1, hits.totalHits);
    assertEquals("wrong doc", 0, hits.scoreDocs[0].doc);
  }

  private void assertBothFoxes(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(2, hits.totalHits);
  }

  private void assertNoMatches(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(0, hits.totalHits);
  }

  public void testSpanTermQuery() throws Exception {
    assertOnlyBrownFox(brown);
    //dumpSpans(brown);
  }

  public void testSpanFirstQuery() throws Exception {
    SpanFirstQuery sfq = new SpanFirstQuery(brown, 2);  //Trova spans che matchano "brown" nel field "field" 2 posizioni dall inizio field stesso.
    assertNoMatches(sfq);								// "the quick brown fox jumps over the lazy dog" brown e' in pos 3
    dumpSpans(sfq);                                   //trova 0 match!!

    sfq = new SpanFirstQuery(brown, 3);                //trova 1 match!!
    dumpSpans(sfq);
    assertOnlyBrownFox(sfq);
  }

  public void testSpanNearQuery() throws Exception {
    SpanQuery[] quick_brown_dog = new SpanQuery[]{quick, brown, dog};
    SpanNearQuery snq = new SpanNearQuery(quick_brown_dog, 0, true);         // #1 Query for three successive terms
    assertNoMatches(snq);													 //slop = 0  =>nessun match
    dumpSpans(snq);

    snq = new SpanNearQuery(quick_brown_dog, 4, true);                       //#2 Same terms, slop of 4
    assertNoMatches(snq);													 //slop = 4  =>nessun match
    dumpSpans(snq);

    snq = new SpanNearQuery(quick_brown_dog, 5, true);                      // #3 SpanNearQuery matches
    assertOnlyBrownFox(snq);
    dumpSpans(snq);															//slop = 5  =>1 match

    // interesting - even a sloppy phrase query would require
    // more slop to match
    snq = new SpanNearQuery(new SpanQuery[]{lazy, fox}, 3, false);         // #4 Nested SpanTermQuery objects in reverse order
    assertOnlyBrownFox(snq);                                               //slop = 3,inOrder=false  =>1 match [lazy e fox sono trovate in ordine invertito e distanziati da 3 terms.]
    dumpSpans(snq);

    PhraseQuery pq = new PhraseQuery();                               		 // #5 Comparable PhraseQuery
    pq.add(new Term("field", "lazy"));                               		 // #5
    pq.add(new Term("field", "fox"));                                		 // #5
    pq.setSlop(4);                                                   		 // #5
    assertNoMatches(pq);                                                      //Non trova nulla dato 3 parole separano  fox e lazy e 2 slops occorrono per avere match le parole vanno inertite.

    pq.setSlop(5);                                                //#6 PhraseQuery, slop of 5 , 1 match
    assertOnlyBrownFox(pq);                                       //#6
  }

  
  /*SpanQueryFilter sembra essere rimosso in Lucene 5 , non testable!
  
  public void testSpanQueryFilter() throws Exception {
    SpanQuery[] quick_brown_dog =new SpanQuery[]{quick, brown, dog};
    SpanQuery snq = new SpanNearQuery(quick_brown_dog, 5, true);
    Filter filter = new SpanQueryFilter(snq);

    Query query = new MatchAllDocsQuery();
    TopDocs hits = searcher.search(query, filter, 10);
    assertEquals(1, hits.totalHits);
    assertEquals("wrong doc", 0, hits.scoreDocs[0].doc);
  }
  */

  public void testSpanNotQuery() throws Exception {
    SpanNearQuery quick_fox =  new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    assertBothFoxes(quick_fox);
    dumpSpans(quick_fox);

    SpanNotQuery quick_fox_dog = new SpanNotQuery(quick_fox, dog);    //gli span quick_fox e dog non si incrociano.I due span vengono ritornati.
    assertBothFoxes(quick_fox_dog);
    dumpSpans(quick_fox_dog);

    SpanNotQuery no_quick_red_fox = new SpanNotQuery(quick_fox, red); //quick_fox include due spans: <quick brown fox> e <quick red fox>.
    assertOnlyBrownFox(no_quick_red_fox);							  //il secondo overlappa con<dog> ergo viene escluso dai results
    dumpSpans(no_quick_red_fox);
  }

  public void testSpanOrQuery() throws Exception {
    SpanNearQuery quick_fox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    SpanNearQuery lazy_dog =  new SpanNearQuery(new SpanQuery[]{lazy, dog}, 0, true);
    SpanNearQuery sleepy_cat =  new SpanNearQuery(new SpanQuery[]{sleepy, cat}, 0, true);

    SpanNearQuery qf_near_ld = new SpanNearQuery(   new SpanQuery[]{quick_fox, lazy_dog}, 3, true);  //<quick brown fox> jumps over the <lazy dog> slop 3 matcha uno span <quick brown fox jumps over the lazy dog>
    assertOnlyBrownFox(qf_near_ld);
    dumpSpans(qf_near_ld);

    SpanNearQuery qf_near_sc =new SpanNearQuery(  new SpanQuery[]{quick_fox, sleepy_cat}, 3, true); //the <quick red fox> jumps over the <sleepy cat> slop 3 metcha uno span <quick red fox jumps over the sleepy cat> 
    dumpSpans(qf_near_sc);

    SpanOrQuery or = new SpanOrQuery( new SpanQuery[]{qf_near_ld, qf_near_sc});//ritorna la ambo gli span di qf_near_ld e qf_near_sc
    assertBothFoxes(or);
    dumpSpans(or);
  }

  public void testPlay() throws Exception {
    SpanOrQuery or = new SpanOrQuery(new SpanQuery[]{quick, fox});
    //dumpSpans(or);

    SpanNearQuery quick_fox =
        new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    SpanFirstQuery sfq = new SpanFirstQuery(quick_fox, 4);
    //dumpSpans(sfq);

    //dumpSpans(new SpanTermQuery(new Term("field", "the")));

    SpanNearQuery quick_brown =
        new SpanNearQuery(new SpanQuery[]{quick, brown}, 0, false);
    //dumpSpans(quick_brown);

  }

  
  
  private void dumpSpans(SpanQuery query) throws IOException { 
    LeafReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
	Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
	Spans spans = query.getSpans(wrapper.getContext(), new Bits.MatchAllBits(reader.numDocs()), termContexts);
    System.out.println(query + ":");
    int numSpans = 0;
    //TopDocs hits = searcher.search(query, 10);
    float[] scores = new float[2];
    //for (ScoreDoc sd : hits.scoreDocs) {
    //  scores[sd.doc] = sd.score;
    //}

    int id = 0;
    while ((id =spans.nextDoc())!= Spans.NO_MORE_DOCS) {                  // A Step through each doc containing at least a  matching spanspan
      Document doc = reader.document(id);  								  // B Retrieve document
      String fieldvalue= doc.get("field");
     
      WhitespaceAnalyzer whitespaceanalyzer = new WhitespaceAnalyzer(); 
      TokenStream stream = whitespaceanalyzer.tokenStream("contents",  new StringReader(fieldvalue));    // C Re-analyze text
      
      
      int span_start_pos= -1;
	  int span_end_pos= -1;
      while((span_start_pos = spans.nextStartPosition())!= Spans.NO_MORE_POSITIONS) {   //Itero per tutti gli span per questo doc
    	  numSpans++;
    	  span_end_pos = spans.endPosition();
	     
	      //analyzer.close();
	      CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
	      stream.reset();      
	      StringBuilder buffer = new StringBuilder();
	      buffer.append("   ");
	      int i = 0;
	     
	      while(stream.incrementToken()) {             // D Step through all tokens
	    	   char[] termBuff= termAtt.buffer();
	    	    int termLen=termAtt.length();
	    	    String currentToken = new String(termBuff,0,termLen);
	    	 
		        if (i == span_start_pos) {          // E Print < and > around span
		          buffer.append("<");              // E
		        }                                  // E
		        buffer.append(currentToken);        // E
		        if (i + 1 == span_end_pos) {        // E
		          buffer.append(">");              // E
		        }                                  // E
		        buffer.append(" ");
		        i++;
		      }
		      buffer.append("(Document Score: ").append(scores[id]).append(") ");
		      System.out.println(buffer);
      }
    }

    if (numSpans == 0) {
      System.out.println("   No spans");
    }
    System.out.println();
  }

  // A Step through each span
  // B Retrieve document
  // C Re-analyze text
  // D Step through all tokens
  // E Print < and > around span
}
