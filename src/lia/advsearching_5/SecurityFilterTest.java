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

import junit.framework.TestCase;
import lia.common.TestUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

// From chapter 5
public class SecurityFilterTest extends TestCase {

  private IndexSearcher searcher;

  protected void setUp() throws Exception {
    Directory directory = new RAMDirectory();
    Analyzer analyzer = new WhitespaceAnalyzer();
    IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
    IndexWriter writer = new IndexWriter(directory, iwConfig);
    IndexReader ireader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(ireader);
    
    
    Document document = new Document();                  
    document.add(new StringField("owner",                      
                           "elwood",                    
                           Field.Store.YES));   
    document.add(new StringField("keywords",                   
                           "elwood's sensitive info",    
                           Field.Store.YES));       
    writer.addDocument(document);

    document = new Document();                          
    document.add(new StringField("owner",                      
                           "jake",                      
                           Field.Store.YES));  
    document.add(new StringField("keywords",                  
                           "jake's sensitive info",      
                           Field.Store.YES));       
    writer.addDocument(document);

    writer.close();
    
  }
  /*
#1 Elwood
#2 Jake
  */

  public void testSecurityFilter() throws Exception {
    TermQuery query = new TermQuery(  new Term("keywords", "info")); 

    assertEquals("Both documents match",  2,  TestUtil.hitCount(searcher, query));  

    Filter jakeFilter = new QueryWrapperFilter(   new TermQuery(new Term("owner", "jake")));     

    TopDocs hits = searcher.search(query, jakeFilter, 10);  //query ritorna 2 docs, jakeFilter solo 1 => searcher ritornera' solo 1!
    assertEquals(1, hits.totalHits);                   //#4
    assertEquals("elwood is safe",                     //#4
                 "jake's sensitive info",              //#4
        searcher.doc(hits.scoreDocs[0].doc)            //#4
                 .get("keywords"));                    //#4
  }
  /*
    #1 TermQuery for "info"
    #2 Returns documents containing "info"
    #3 Filter
    #4 Same TermQuery, constrained results
  */
}
