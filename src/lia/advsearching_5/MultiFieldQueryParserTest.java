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

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

// From chapter 5
public class MultiFieldQueryParserTest extends TestCase {
	
	//Trova tutti i Doc con fields  title o subject che abbiano "development" dentro  title OR subject
  public void testDefaultOperator() throws Exception {
    Query query = new MultiFieldQueryParser( new String[]{"title", "subject"},
        new StandardAnalyzer()).parse("development");

    Directory dir = TestUtil.getBookIndexDirectory();
    IndexReader ireader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(ireader);
    TopDocs hits = searcher.search(query, 10);

    assertTrue(TestUtil.hitsIncludeTitle(
           searcher,
           hits,
           "Ant in Action"));

    assertTrue(TestUtil.hitsIncludeTitle(     //A
           searcher,                          //A
           hits,                              //A
           "Extreme Programming Explained")); //A
    //searcher.close();
    dir.close();
  }

  public void testSpecifiedOperator() throws Exception {
	  //Trova tutti i Doc con fields che abbiano "lucene" dentro ambo title AND subject
    Query query = MultiFieldQueryParser.parse(
        "lucene",
        new String[]{"title", "subject"},
        new BooleanClause.Occur[]{BooleanClause.Occur.MUST,
                  BooleanClause.Occur.MUST},
        new SimpleAnalyzer());

    Directory dir = TestUtil.getBookIndexDirectory();
    IndexReader ireader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(ireader);
    TopDocs hits = searcher.search(query, 10);

    assertTrue(TestUtil.hitsIncludeTitle(
            searcher,
            hits,
            "Lucene in Action, Second Edition"));
    assertEquals("one and only one", 1, hits.scoreDocs.length);
    //searcher.close();
    dir.close();
  }

  /*
    #A Has development in the subject field
   */

}
