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

import junit.framework.TestCase;
import lia.common.TestUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

// From chapter 3
public class QueryParserTest extends TestCase {

  private Analyzer analyzer;
  private Directory dir;
  private IndexSearcher searcher;

  protected void setUp() throws Exception {
    analyzer = new WhitespaceAnalyzer();
    dir = TestUtil.getBookIndexDirectory();
    IndexReader ireader = DirectoryReader.open(dir);
    searcher = new IndexSearcher(ireader);  //B
  }

  protected void tearDown() throws Exception {
    //searcher.close();
    dir.close();
  }

  public void testToString() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add(new FuzzyQuery(new Term("field", "kountry")),
              BooleanClause.Occur.MUST);
    query.add(new TermQuery(new Term("title", "western")),
              BooleanClause.Occur.SHOULD);
    assertEquals("both kinds", "+kountry~0.5 title:western",
                 query.toString("field"));
  }

  public void testPrefixQuery() throws Exception {
    QueryParser parser = new QueryParser("category", new StandardAnalyzer());
    parser.setLowercaseExpandedTerms(false);
    System.out.println(parser.parse("/Computers/technology*").toString("category"));
  }

  public void testFuzzyQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("kountry~");
    System.out.println("fuzzy: " + query);

    query = parser.parse("kountry~0.7");
    System.out.println("fuzzy 2: " + query);
  }

  public void testGrouping() throws Exception {
    Query query = new QueryParser(
        "subject",
        analyzer)
    //.parse("(agile OR extreme) AND methodology");
    .parse("+title:\"The Pragmatic Programmer\" AND (subject:agile OR subject:extreme OR pippo)");
    TopDocs matches = searcher.search(query, 10);

    System.out.println(query + " , hits = "+matches.totalHits);
    for(int i=0;i<matches.totalHits;i++) {
    	Document doc = searcher.doc(matches.scoreDocs[i].doc);
        System.out.println("match " + i + ": subject = " + doc.get("subject") + "; title = " +doc.get("title"));
      }
  
    assertTrue(TestUtil.hitsIncludeTitle(searcher,
                                         matches,
                                         "The Pragmatic Programmer"));
    
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
            "Extreme Programming Explained"));
  }

  public void testTermQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("computers");
    System.out.println("term: " + query);
  }

  public void testTermRangeQuery() throws Exception {
    Query query = new QueryParser(                        //A
                                  "subject", analyzer).parse("title2:[Q TO V]"); //A
    assertTrue(query instanceof TermRangeQuery);

    TopDocs matches = searcher.search(query, 10);
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                      "Tapestry in Action"));

    query = new QueryParser( "subject", analyzer)  //B
                            .parse("title2:{Q TO \"Tapestry in Action\"}");    //B
    matches = searcher.search(query, 10);
    assertFalse(TestUtil.hitsIncludeTitle(searcher, matches,  // C
                      "Tapestry in Action"));
  }
  /*
    #A Verify inclusive range
    #B Verify exclusive range
    #C Exclude Mindstorms book
  */

  public void testPhraseQuery() throws Exception {
    Query q = new QueryParser(
                              "field",
                              new StandardAnalyzer())
                .parse("\"This is Some Phrase*\"");
    assertEquals("analyzed",
        "\"? ? some phrase\"", q.toString("field"));

    q = new QueryParser(
                        "field", analyzer).parse("\"term\"");
    assertTrue("reduced to TermQuery", q instanceof TermQuery);
  }

  public void testSlop() throws Exception {
    Query q = new QueryParser(
                              "field", analyzer)
            .parse("\"exact phrase\"");
    assertEquals("zero slop",
        "\"exact phrase\"", q.toString("field"));

    QueryParser qp = new QueryParser(
                                     "field", analyzer);
    qp.setPhraseSlop(5);
    q = qp.parse("\"sloppy phrase\"");
    assertEquals("sloppy, implicitly",
        "\"sloppy phrase\"~5", q.toString("field"));
  }

  public void testLowercasing() throws Exception {
    Query q = new QueryParser( "field", analyzer).parse("PrefixQuery*");
    assertEquals("lowercased", "prefixquery*", q.toString("field"));

    QueryParser qp = new QueryParser(  "field", analyzer);
    qp.setLowercaseExpandedTerms(false);
    q = qp.parse("PrefixQuery*");
    assertEquals("not lowercased",
        "PrefixQuery*", q.toString("field"));
  }

  public void testWildcard() {
    try {
      new QueryParser(
                      "field", analyzer).parse("*xyz");
      fail("Leading wildcard character should not be allowed");
    } catch (ParseException expected) {
      assertTrue(true);
    }
  }

  public void testBoost() throws Exception {
    Query q = new QueryParser("field", analyzer).parse("term^2");
    assertEquals("term^2.0", q.toString("field"));
  }

  public void testParseException() {
    try {
      new QueryParser(
                      "contents", analyzer).parse("^&#");
    } catch (ParseException expected) {
      // expression is invalid, as expected
      assertTrue(true);
      return;
    }

    fail("ParseException expected, but not thrown");
  }
}
