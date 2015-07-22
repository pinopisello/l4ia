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

import junit.framework.TestCase;
import lia.common.TestUtil;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

// From chapter 5
public class FilterTest extends TestCase {
  private Query allBooks;
  private IndexSearcher searcher;
  private Directory dir;
  private IndexReader reader;
  
  protected void setUp() throws Exception {    // #1
    allBooks = new MatchAllDocsQuery();
    dir = TestUtil.getBookIndexDirectory();
    reader = DirectoryReader.open(dir);
    searcher = new IndexSearcher(reader);
  }

  protected void tearDown() throws Exception {
    dir.close();
  }

  

  
  public void testTermRangeFilter() throws Exception {
    Filter filter =  TermRangeFilter.newStringRange("title2", "d", "g", true, true); //Fa passare solo i docs con field "title2" contenente 
    																				 //terms che iniziano per lettere tra 'd' e 'j'
    TopDocs result = searcher.search(allBooks, filter,50);
    System.out.println("testTermRangeFilter hits: "+result.totalHits);
   for(ScoreDoc currmatch : result.scoreDocs){
	   Document doc = reader.document(currmatch.doc);
	   System.out.println(doc.get("title2"));
   }
    assertEquals(3, TestUtil.hitCount(searcher, allBooks, filter));
  }

  /*
    #1 setUp() establishes baseline book count
  */

  public void testNumericDateFilter() throws Exception {
    // pub date of Lucene in Action, Second Edition and
    // JUnit in ACtion, Second Edition is May 2010
    Filter filter = NumericRangeFilter.newIntRange("pubmonth", //lascia passare solo docs con jen-2010 < pubmonth < june-2010
                                                   201001,
                                                   201006,
                                                   true,
                                                   true);
    TopDocs result = searcher.search(allBooks, filter,50); 
    System.out.println("testNumericDateFilter hits: "+result.totalHits);
    for(ScoreDoc currmatch : result.scoreDocs){
 	   Document doc = reader.document(currmatch.doc);
 	   System.out.println(doc.get("title")+"  "+doc.get("pubmonth"));
    }
    assertEquals(2, TestUtil.hitCount(searcher, allBooks, filter));
  }
  
  
/*
  public void testFieldCacheRangeFilter() throws Exception {
    Filter filter = FieldCacheRangeFilter.newStringRange("title2", "d", "j", true, true);
    assertEquals(3, TestUtil.hitCount(searcher, allBooks, filter));

    filter = FieldCacheRangeFilter.newIntRange("pubmonth",
                                               201001,
                                               201006,
                                               true,
                                               true);
    assertEquals(2, TestUtil.hitCount(searcher, allBooks, filter));
  }
  
  
  public void testFieldCacheTermsFilter() throws Exception {
    Filter filter = new FieldCacheTermsFilter("category",
                      new String[] {"/health/alternative/chinese",
                                    "/technology/computers/ai",
                                    "/technology/computers/programming"});
    assertEquals("expected 7 hits",
                 7,
                 TestUtil.hitCount(searcher, allBooks, filter));
  }
*/
  public void testQueryWrapperFilter() throws Exception {
    TermQuery categoryQuery = new TermQuery(new Term("category", "/philosophy/eastern"));
    Filter categoryFilter = new QueryWrapperFilter(categoryQuery);  // i docs ritornati da categoryQuery vanno sovrapposti con quelli della main query [allBooks in questo caso]
   
    TopDocs result = searcher.search(allBooks, categoryFilter,50); //ritorna tutti i doc che matchano query "allBooks" e "categoryFilter" insieme
    System.out.println("testQueryWrapperFilter hits: "+result.totalHits);
    for(ScoreDoc currmatch : result.scoreDocs){
 	   Document doc = reader.document(currmatch.doc);
 	   System.out.println(doc.get("title")+"  "+doc.get("category"));
    }
    assertEquals("only tao te ching",
                 1,
                 TestUtil.hitCount(searcher, allBooks, categoryFilter));
  }

  /*
  public void testSpanQueryFilter() throws Exception {
    SpanQuery categoryQuery =
       new SpanTermQuery(new Term("category", "/philosophy/eastern"));

    Filter categoryFilter = new SpanQueryFilter(categoryQuery);

    assertEquals("only tao te ching",
                 1,
                 TestUtil.hitCount(searcher, allBooks, categoryFilter));
  }
*/
  public void testFilterAlternative() throws Exception {
    TermQuery categoryQuery = new TermQuery(new Term("category", "/philosophy/eastern"));  //Fa passare solo i docs con field "category" ==  /philosophy/eastern                 

    BooleanQuery constrainedQuery = new BooleanQuery();
    constrainedQuery.add(allBooks, BooleanClause.Occur.MUST);
    constrainedQuery.add(categoryQuery, BooleanClause.Occur.MUST);

    TopDocs result = searcher.search( constrainedQuery,50);    //data la query iniziale allBooks, si costruisce una BooleanQuery == allBooks && categoryQuery 
    System.out.println("testFilterAlternative hits: "+result.totalHits);
    for(ScoreDoc currmatch : result.scoreDocs){
 	   Document doc = reader.document(currmatch.doc);
 	   System.out.println(doc.get("title")+"  "+doc.get("category"));
    }
    assertEquals("only tao te ching",1,TestUtil.hitCount(searcher, constrainedQuery));
  }

  public void testPrefixFilter() throws Exception {
    Filter prefixFilter = new PrefixFilter( new Term("category", "/technology/computers"));  //Fa passare solo i docs con field "category" che inizia per  /technology/computers                                  
    TopDocs result = searcher.search(allBooks, prefixFilter,50); //ritorna tutti i doc che matchano query "allBooks" e "prefixFilter" insieme
    System.out.println("testPrefixFilter hits: "+result.totalHits);
    for(ScoreDoc currmatch : result.scoreDocs){
 	   Document doc = reader.document(currmatch.doc);
 	   System.out.println(doc.get("title")+"  "+doc.get("category"));
    }
    assertEquals("only /technology/computers/* books",
                 8,
                 TestUtil.hitCount(searcher,
                                   allBooks,
                                   prefixFilter));
  }

  public void testCachingWrapper() throws Exception {
    Filter filter =  TermRangeFilter.newStringRange("title2", "d", "j", true, true);

    CachingWrapperFilter cachingFilter;
    cachingFilter = new CachingWrapperFilter(filter);
    assertEquals(3, TestUtil.hitCount(searcher, allBooks,cachingFilter));
  }
}
